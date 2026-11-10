package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Parada;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Punto;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Resumen;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.TareaRecorrido;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.PosicionTareaHistoricoRepository;
import com.soprint.seguimiento_mensajeros.repository.PosicionTareaHistoricoRepository.PuntoRecorrido;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Arma el recorrido de un mensajero en un día a partir de las posiciones que manda la app
 * (una cada ~30 s según datos de producción, nov. 2026).
 *
 * El GPS del celular mete ruido: saltos de cientos de metros y vibración estando quieto.
 * Por eso se filtran los saltos imposibles, los km se cuentan solo cuando el mensajero se
 * aleja de verdad del último punto contado y las paradas se detectan por radio + tiempo.
 */
@Service
public class RecorridoService {

    /** Más rápido que esto entre dos puntos es un salto del GPS, no movimiento real (~160 km/h). */
    static final double VELOCIDAD_MAXIMA_MS = 45.0;
    /** Un salto solo se descarta si además es grande; los saltos cortos los absorbe el resto. */
    static final double SALTO_MINIMO_M = 200.0;
    /** Pasado este tiempo sin datos no se juzga la velocidad: puede haber viajado de verdad. */
    static final Duration HUECO_SIN_FILTRO = Duration.ofMinutes(10);
    /** Con tantos saltos seguidos descartados, el punto "bueno" era el anterior y se acepta el nuevo. */
    static final int MAX_DESCARTES_SEGUIDOS = 3;

    /** Hueco: este tiempo sin posiciones y reaparece lejos. El tramo no es el camino real. */
    static final Duration HUECO_MINIMO = Duration.ofMinutes(5);
    static final double HUECO_DISTANCIA_M = 500.0;

    /** Para los km: se suma recién cuando se aleja esto del último punto contado (evita la vibración). */
    static final double PASO_KM_M = 30.0;

    /** Parada: quieto dentro de este radio al menos este tiempo. */
    static final double PARADA_RADIO_M = 75.0;
    static final Duration PARADA_MINIMA = Duration.ofMinutes(5);

    private final PosicionTareaHistoricoRepository historicoRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;

    public RecorridoService(PosicionTareaHistoricoRepository historicoRepository,
            TareaRepository tareaRepository,
            UsuarioRepository usuarioRepository) {
        this.historicoRepository = historicoRepository;
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public RecorridoDTO obtenerRecorrido(Long idMensajero, LocalDate fecha, LocalTime horaDesde, LocalTime horaHasta) {
        Usuario mensajero = usuarioRepository.findById(idMensajero)
                .orElseThrow(() -> new IllegalArgumentException("Mensajero no encontrado: " + idMensajero));

        LocalDateTime desde = fecha.atTime(horaDesde != null ? horaDesde : LocalTime.MIN);
        LocalDateTime hasta = horaHasta != null ? fecha.atTime(horaHasta) : fecha.plusDays(1).atStartOfDay();

        List<PuntoRecorrido> crudos = historicoRepository.findRecorrido(idMensajero, desde, hasta);
        List<Punto> puntos = limpiarYMarcarCortes(crudos);
        List<Parada> paradas = detectarParadas(puntos);

        Set<Long> idsConPosicion = puntos.stream().map(Punto::idTarea).filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        // Postgres no acepta "IN ()": con la lista vacía se manda un id que no existe.
        List<Long> ids = idsConPosicion.isEmpty() ? List.of(-1L) : new ArrayList<>(idsConPosicion);
        List<TareaRecorrido> tareas = tareaRepository.buscarParaRecorrido(idMensajero, ids, desde, hasta).stream()
                .map(RecorridoService::aTareaRecorrido)
                .sorted(Comparator.comparing(
                        (TareaRecorrido t) -> primeraHora(t, puntos),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        String nombre = (Objects.toString(mensajero.getNombre(), "") + " "
                + Objects.toString(mensajero.getApellido(), "")).trim();

        return new RecorridoDTO(idMensajero, nombre, fecha, puntos, tareas, paradas, resumir(puntos, paradas));
    }

    // ===== Limpieza =====

    /** Puntos que llegan con menos de esta diferencia vienen de la misma ráfaga. */
    static final Duration RAFAGA = Duration.ofSeconds(2);

    /**
     * Ráfagas: con el celular en segundo plano, Android junta varias posiciones y la app
     * (Movil-SGR, LocationService) las manda todas a la vez y en paralelo. El servidor les
     * pone la hora de llegada (PosicionRequest no trae la hora del GPS), así que llegan
     * con la misma hora y en desorden. Las posiciones son reales: se ordenan por cercanía
     * empezando desde el punto anterior y se reparte entre ellas el tiempo transcurrido
     * desde ese punto. Sin esto se veían como saltos imposibles y se descartaban, o se
     * dibujaba un zigzag que inflaba los km.
     */
    static List<PuntoRecorrido> desagruparRafagas(List<PuntoRecorrido> crudos) {
        List<PuntoRecorrido> validos = crudos.stream()
                .filter(c -> c.latitud() != null && c.longitud() != null
                        && !(c.latitud() == 0 && c.longitud() == 0)
                        && c.fechaRegistro() != null)
                .toList();

        List<PuntoRecorrido> salida = new ArrayList<>();
        int i = 0;
        while (i < validos.size()) {
            int j = i;
            while (j + 1 < validos.size()
                    && Duration.between(validos.get(i).fechaRegistro(), validos.get(j + 1).fechaRegistro())
                            .compareTo(RAFAGA) < 0) {
                j++;
            }
            if (j == i) {
                salida.add(validos.get(i));
                i++;
                continue;
            }

            List<PuntoRecorrido> pendientes = new ArrayList<>(validos.subList(i, j + 1));
            PuntoRecorrido referencia = salida.isEmpty() ? pendientes.get(0) : salida.get(salida.size() - 1);
            List<PuntoRecorrido> ordenados = new ArrayList<>();
            while (!pendientes.isEmpty()) {
                PuntoRecorrido ref = referencia;
                PuntoRecorrido masCerca = Collections.min(pendientes, Comparator.comparingDouble(
                        p -> distanciaMetros(ref.latitud(), ref.longitud(), p.latitud(), p.longitud())));
                pendientes.remove(masCerca);
                ordenados.add(masCerca);
                referencia = masCerca;
            }

            LocalDateTime fin = ordenados.get(ordenados.size() - 1).fechaRegistro();
            LocalDateTime inicio = salida.isEmpty() ? fin : salida.get(salida.size() - 1).fechaRegistro();
            if (inicio.isAfter(fin)) {
                inicio = fin;
            }
            long nanos = Duration.between(inicio, fin).toNanos();
            int n = ordenados.size();
            for (int k = 0; k < n; k++) {
                PuntoRecorrido p = ordenados.get(k);
                salida.add(new PuntoRecorrido(p.latitud(), p.longitud(),
                        inicio.plusNanos(nanos * (k + 1) / n), p.idTarea()));
            }
            i = j + 1;
        }
        return salida;
    }

    static List<Punto> limpiarYMarcarCortes(List<PuntoRecorrido> crudos) {
        List<Punto> salida = new ArrayList<>();
        Punto anterior = null;
        int descartesSeguidos = 0;

        for (PuntoRecorrido c : desagruparRafagas(crudos)) {
            if (anterior == null) {
                anterior = new Punto(c.latitud(), c.longitud(), c.fechaRegistro(), c.idTarea(), false);
                salida.add(anterior);
                continue;
            }

            double distancia = distanciaMetros(anterior.lat(), anterior.lng(), c.latitud(), c.longitud());
            Duration dt = Duration.between(anterior.hora(), c.fechaRegistro());
            double segundos = Math.max(dt.toMillis() / 1000.0, 1.0);

            boolean saltoImposible = dt.compareTo(HUECO_SIN_FILTRO) < 0
                    && distancia > SALTO_MINIMO_M
                    && distancia / segundos > VELOCIDAD_MAXIMA_MS;
            if (saltoImposible && descartesSeguidos < MAX_DESCARTES_SEGUIDOS) {
                descartesSeguidos++;
                continue;
            }
            descartesSeguidos = 0;

            boolean corte = dt.compareTo(HUECO_MINIMO) >= 0 && distancia >= HUECO_DISTANCIA_M;
            anterior = new Punto(c.latitud(), c.longitud(), c.fechaRegistro(), c.idTarea(), corte);
            salida.add(anterior);
        }
        return salida;
    }

    // ===== Paradas =====

    static List<Parada> detectarParadas(List<Punto> puntos) {
        List<Parada> paradas = new ArrayList<>();
        int i = 0;
        while (i < puntos.size()) {
            Punto centro = puntos.get(i);
            int j = i;
            while (j + 1 < puntos.size()
                    && distanciaMetros(centro.lat(), centro.lng(), puntos.get(j + 1).lat(), puntos.get(j + 1).lng())
                            <= PARADA_RADIO_M) {
                j++;
            }

            Duration duracion = Duration.between(centro.hora(), puntos.get(j).hora());
            if (j > i && duracion.compareTo(PARADA_MINIMA) >= 0) {
                List<Punto> grupo = puntos.subList(i, j + 1);
                double lat = grupo.stream().mapToDouble(Punto::lat).average().orElse(centro.lat());
                double lng = grupo.stream().mapToDouble(Punto::lng).average().orElse(centro.lng());
                paradas.add(new Parada(lat, lng, centro.hora(), puntos.get(j).hora(), duracion.toMinutes(),
                        tareaMasFrecuente(grupo)));
                i = j + 1;
            } else {
                i++;
            }
        }
        return paradas;
    }

    private static Long tareaMasFrecuente(List<Punto> grupo) {
        return grupo.stream().map(Punto::idTarea).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    // ===== Resumen =====

    static Resumen resumir(List<Punto> puntos, List<Parada> paradas) {
        if (puntos.isEmpty()) {
            return new Resumen(0, 0, null, null, 0, 0, 0, 0);
        }
        // Los tramos con corte se cuentan en línea recta: es lo mínimo que recorrió.
        double metros = 0;
        Punto ancla = puntos.get(0);
        for (Punto p : puntos) {
            double d = distanciaMetros(ancla.lat(), ancla.lng(), p.lat(), p.lng());
            if (d >= PASO_KM_M) {
                metros += d;
                ancla = p;
            }
        }

        LocalDateTime primera = puntos.get(0).hora();
        LocalDateTime ultima = puntos.get(puntos.size() - 1).hora();
        long jornada = Duration.between(primera, ultima).toMinutes();
        long parado = paradas.stream().mapToLong(Parada::minutos).sum();
        int cortes = (int) puntos.stream().filter(Punto::corte).count();

        return new Resumen(puntos.size(), Math.round(metros / 100.0) / 10.0, primera, ultima,
                jornada, parado, Math.max(jornada - parado, 0), cortes);
    }

    // ===== Utilidades =====

    private static TareaRecorrido aTareaRecorrido(Tarea t) {
        var c = t.getCliente();
        return new TareaRecorrido(
                t.getIdTarea(),
                t.getCodigo(),
                t.getNombre(),
                t.getTipoOperacion() != null ? t.getTipoOperacion().getNombre() : null,
                c != null ? c.getNombre() : null,
                c != null ? c.getLatitud() : null,
                c != null ? c.getLongitud() : null,
                t.getFechaInicio(),
                t.getFechaFin());
    }

    /** Para ordenar las tareas en el orden en que se hicieron. */
    private static LocalDateTime primeraHora(TareaRecorrido t, List<Punto> puntos) {
        return puntos.stream().filter(p -> t.idTarea().equals(p.idTarea())).map(Punto::hora).findFirst()
                .orElse(t.fechaInicio() != null ? t.fechaInicio() : t.fechaFin());
    }

    /** Distancia en metros entre dos coordenadas (haversine). */
    static double distanciaMetros(double lat1, double lng1, double lat2, double lng2) {
        double r = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }
}
