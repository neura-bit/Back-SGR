package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoVehiculo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Evaluacion de mensajeros contra pares que hicieron el mismo tipo de
 * trabajo.
 *
 * Una garantia o un servicio tecnico no se cumplen igual que una entrega, y
 * llegar a tiempo en camion no cuesta lo mismo que en moto. Comparar a todos
 * contra el mismo porcentaje castiga al que recibe el trabajo mas dificil,
 * sobre todo a los tecnicos, que estan dados de alta como mensajeros.
 *
 * Cada tarea se compara contra las del resto de la flota, buscando de la
 * comparacion mas precisa a la mas general hasta encontrar al menos
 * {@link #MIN_TAREAS_FLOTA} tareas de otros:
 * <ol>
 * <li>mismo tipo de operacion y mismo vehiculo;</li>
 * <li>mismo tipo de operacion, cualquier vehiculo;</li>
 * <li>misma area (tecnica o logistica).</li>
 * </ol>
 * Con la comparacion solo por vehiculo, quien hacia la mayoria de las tareas
 * de un vehiculo se quedaba sin pares y esas tareas no contaban; ahora caen
 * al siguiente nivel.
 *
 * Se descarto a proposito usar factores fijos ("la moto es 1.4 veces mas
 * rapida"): la referencia sale de los datos reales, se recalcula en cada
 * consulta y se expone para que se pueda auditar.
 *
 * La referencia de cada mensajero excluye sus propias tareas. Si no, el
 * unico tecnico de una sucursal seria su propia referencia y siempre
 * quedaria "igual que sus pares".
 *
 * No depende de Spring ni de la base: recibe registros y devuelve numeros,
 * para poder probarla sola.
 */
public final class BaselinePares {

    /** Tareas de otros mensajeros en una comparacion para confiar en su promedio. */
    public static final int MIN_TAREAS_FLOTA = 20;

    /** Tareas del propio mensajero con referencia valida para darle un indice. */
    public static final int MIN_TAREAS_MENSAJERO = 5;

    public enum Area {
        LOGISTICA("Logística"),
        TECNICA("Técnica");

        private final String etiqueta;

        Area(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        static Area de(boolean tecnico) {
            return tecnico ? TECNICA : LOGISTICA;
        }
    }

    /** Con que se comparo una tarea, de la mas precisa a la mas general. */
    public enum Nivel {
        TIPO_Y_VEHICULO("Mismo tipo y vehículo"),
        TIPO("Mismo tipo"),
        AREA("Misma área");

        private final String etiqueta;

        Nivel(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    /** Una tarea finalizada con vehiculo declarado. */
    public record Registro(Long idMensajero, Long idTipoOperacion, String tipoOperacion, boolean tecnica,
            TipoVehiculo vehiculo, Boolean aTiempo, Long minutosEjecucion) {
    }

    /** Lo que logro la flota del alcance con un vehiculo dentro de un tipo. */
    public record ReferenciaVehiculo(TipoVehiculo vehiculo, int tareas, Double porcentajeATiempo,
            boolean suficiente) {
    }

    /**
     * Lo que logro la flota del alcance en un tipo de operacion. suficiente
     * significa que todos los que hicieron ese tipo se comparan al menos
     * contra el tipo; si es false, alguno cae a la comparacion por area.
     */
    public record ReferenciaTipo(Long idTipoOperacion, String nombre, Area area, int tareas, int mensajeros,
            Double porcentajeATiempo, Double minutosEjecucionPromedio, boolean suficiente,
            List<ReferenciaVehiculo> vehiculos) {
    }

    public record ReferenciaArea(Area area, int tareas, int mensajeros, Double porcentajeATiempo,
            Double minutosEjecucionPromedio) {
    }

    /**
     * Resultado por mensajero. Los campos quedan en null cuando no hay base
     * suficiente: preferimos "sin base" a un numero que no significa nada.
     */
    public record Evaluacion(
            /** Area donde hizo la mayoria de sus tareas; define en que ranking aparece. */
            Area area,
            /** Tareas del mensajero que entraron al calculo del cumplimiento. */
            int tareasEvaluadas,
            /** % a tiempo del mensajero sobre esas mismas tareas. */
            Double cumplimientoReal,
            /** % que habria logrado un par promedio con su misma mezcla de trabajo. */
            Double cumplimientoEsperado,
            /** real - esperado, en puntos porcentuales. */
            Double diferenciaVsPares,
            /** Cumplimiento llevado a la escala de la flota: general + diferencia. */
            Double cumplimientoAjustado,
            /** Tiempo de ejecucion esperado / real. 1.00 = ritmo de sus pares. */
            Double indiceEficiencia,
            /** Cuantas tareas hizo con cada vehiculo (todas las declaradas). */
            Map<TipoVehiculo, Integer> tareasPorVehiculo,
            /** Cuantas tareas hizo de cada tipo de operacion, por nombre. */
            Map<String, Integer> tareasPorTipo,
            /** Contra que se comparo cada una de las tareas evaluadas. */
            Map<Nivel, Integer> tareasPorNivel) {
    }

    /** Acumulador de una celda (tipo y vehiculo, tipo o area) de la flota o de un mensajero. */
    private static final class Acumulado {
        int tareas;
        int conPlazo;
        int aTiempo;
        int conEjecucion;
        long minutos;

        void sumar(Registro r) {
            tareas++;
            if (r.aTiempo() != null) {
                conPlazo++;
                if (r.aTiempo()) {
                    aTiempo++;
                }
            }
            if (r.minutosEjecucion() != null) {
                conEjecucion++;
                minutos += r.minutosEjecucion();
            }
        }

        Double porcentajeATiempo() {
            return conPlazo > 0 ? aTiempo * 100.0 / conPlazo : null;
        }

        Double minutosPromedio() {
            return conEjecucion > 0 ? (double) minutos / conEjecucion : null;
        }
    }

    /** Totales de la flota y de cada mensajero para una forma de agrupar. */
    private static final class Agrupacion<K> {
        final Function<Registro, K> clave;
        final Map<K, Acumulado> flota = new LinkedHashMap<>();
        final Map<Long, Map<K, Acumulado>> porMensajero = new HashMap<>();

        Agrupacion(Function<Registro, K> clave) {
            this.clave = clave;
        }

        void sumar(Registro r) {
            K k = clave.apply(r);
            flota.computeIfAbsent(k, x -> new Acumulado()).sumar(r);
            porMensajero.computeIfAbsent(r.idMensajero(), id -> new HashMap<>())
                    .computeIfAbsent(k, x -> new Acumulado()).sumar(r);
        }

        Acumulado propio(Long idMensajero, K k) {
            return porMensajero.getOrDefault(idMensajero, Map.of()).getOrDefault(k, new Acumulado());
        }

        int mensajeros(K k) {
            return (int) porMensajero.values().stream().filter(m -> m.containsKey(k)).count();
        }

        /** Todos los que hicieron tareas en la celda tienen al menos el minimo de otros. */
        boolean suficiente(K k) {
            int mayorPropio = porMensajero.values().stream()
                    .map(m -> m.get(k))
                    .filter(a -> a != null)
                    .mapToInt(a -> a.conPlazo)
                    .max()
                    .orElse(0);
            return flota.get(k).conPlazo - mayorPropio >= MIN_TAREAS_FLOTA;
        }
    }

    private record TipoYVehiculo(Long idTipoOperacion, TipoVehiculo vehiculo) {
    }

    private final Agrupacion<TipoYVehiculo> porTipoYVehiculo =
            new Agrupacion<>(r -> new TipoYVehiculo(r.idTipoOperacion(), r.vehiculo()));
    private final Agrupacion<Long> porTipo = new Agrupacion<>(Registro::idTipoOperacion);
    private final Agrupacion<Area> porArea = new Agrupacion<>(r -> Area.de(r.tecnica()));
    private final Map<Long, String> nombresTipo = new HashMap<>();
    private final Map<Long, Area> areasTipo = new HashMap<>();
    private final Acumulado general = new Acumulado();

    public BaselinePares(List<Registro> registros) {
        for (Registro r : registros) {
            if (r.vehiculo() == null || r.idMensajero() == null || r.idTipoOperacion() == null) {
                continue;
            }
            porTipoYVehiculo.sumar(r);
            porTipo.sumar(r);
            porArea.sumar(r);
            general.sumar(r);
            nombresTipo.putIfAbsent(r.idTipoOperacion(), r.tipoOperacion());
            areasTipo.putIfAbsent(r.idTipoOperacion(), Area.de(r.tecnica()));
        }
    }

    /** % a tiempo de toda la flota del alcance, sin distinguir trabajo. */
    public Double getPorcentajeGeneral() {
        return redondear(general.porcentajeATiempo());
    }

    /** La tabla de referencia por tipo de operacion que se muestra en pantalla y en el PDF. */
    public List<ReferenciaTipo> referenciasPorTipo() {
        List<ReferenciaTipo> lista = new ArrayList<>();
        for (Map.Entry<Long, Acumulado> e : porTipo.flota.entrySet()) {
            Long idTipo = e.getKey();
            Acumulado a = e.getValue();
            List<ReferenciaVehiculo> vehiculos = porTipoYVehiculo.flota.entrySet().stream()
                    .filter(v -> v.getKey().idTipoOperacion().equals(idTipo))
                    .sorted(Comparator.comparingInt((Map.Entry<TipoYVehiculo, Acumulado> v) -> v.getValue().tareas)
                            .reversed())
                    .map(v -> new ReferenciaVehiculo(
                            v.getKey().vehiculo(),
                            v.getValue().tareas,
                            redondear(v.getValue().porcentajeATiempo()),
                            porTipoYVehiculo.suficiente(v.getKey())))
                    .toList();
            lista.add(new ReferenciaTipo(
                    idTipo,
                    nombresTipo.get(idTipo),
                    areasTipo.get(idTipo),
                    a.tareas,
                    porTipo.mensajeros(idTipo),
                    redondear(a.porcentajeATiempo()),
                    redondear(a.minutosPromedio()),
                    porTipo.suficiente(idTipo),
                    vehiculos));
        }
        lista.sort(Comparator.comparing(ReferenciaTipo::area).thenComparing(ReferenciaTipo::tareas,
                Comparator.reverseOrder()));
        return lista;
    }

    public List<ReferenciaArea> referenciasPorArea() {
        List<ReferenciaArea> lista = new ArrayList<>();
        for (Area area : Area.values()) {
            Acumulado a = porArea.flota.get(area);
            if (a != null) {
                lista.add(new ReferenciaArea(area, a.tareas, porArea.mensajeros(area),
                        redondear(a.porcentajeATiempo()), redondear(a.minutosPromedio())));
            }
        }
        return lista;
    }

    public Evaluacion evaluar(Long idMensajero) {
        Map<TipoYVehiculo, Acumulado> propias = porTipoYVehiculo.porMensajero.getOrDefault(idMensajero, Map.of());

        Map<TipoVehiculo, Integer> tareasPorVehiculo = new EnumMap<>(TipoVehiculo.class);
        Map<String, Integer> tareasPorTipo = new LinkedHashMap<>();
        int tareasTecnicas = 0;
        int tareasLogisticas = 0;
        for (Map.Entry<TipoYVehiculo, Acumulado> e : propias.entrySet()) {
            int n = e.getValue().tareas;
            tareasPorVehiculo.merge(e.getKey().vehiculo(), n, Integer::sum);
            tareasPorTipo.merge(nombresTipo.get(e.getKey().idTipoOperacion()), n, Integer::sum);
            if (areasTipo.get(e.getKey().idTipoOperacion()) == Area.TECNICA) {
                tareasTecnicas += n;
            } else {
                tareasLogisticas += n;
            }
        }
        // Empate o sin tareas: logistica, que es el puesto para el que se da de alta a un mensajero
        Area area = tareasTecnicas > tareasLogisticas ? Area.TECNICA : Area.LOGISTICA;

        // Cumplimiento: cada tarea con plazo se compara contra el % a tiempo
        // de otros mensajeros en la comparacion mas precisa que tenga base.
        int evaluadas = 0;
        int aTiempo = 0;
        double esperadoAcumulado = 0;
        Map<Nivel, Integer> tareasPorNivel = new EnumMap<>(Nivel.class);

        // Eficiencia: minutos que le habrian tomado a sus pares frente a los reales.
        int conEjecucion = 0;
        double minutosEsperados = 0;
        long minutosReales = 0;

        for (Map.Entry<TipoYVehiculo, Acumulado> e : propias.entrySet()) {
            Acumulado mio = e.getValue();
            TipoYVehiculo celda = e.getKey();
            Area areaTipo = areasTipo.get(celda.idTipoOperacion());
            List<Comparacion> comparaciones = List.of(
                    new Comparacion(Nivel.TIPO_Y_VEHICULO, porTipoYVehiculo.flota.get(celda),
                            porTipoYVehiculo.propio(idMensajero, celda)),
                    new Comparacion(Nivel.TIPO, porTipo.flota.get(celda.idTipoOperacion()),
                            porTipo.propio(idMensajero, celda.idTipoOperacion())),
                    new Comparacion(Nivel.AREA, porArea.flota.get(areaTipo),
                            porArea.propio(idMensajero, areaTipo)));

            if (mio.conPlazo > 0) {
                for (Comparacion c : comparaciones) {
                    int plazoOtros = c.flota().conPlazo - c.propio().conPlazo;
                    if (plazoOtros >= MIN_TAREAS_FLOTA) {
                        double tasaOtros = (double) (c.flota().aTiempo - c.propio().aTiempo) / plazoOtros;
                        evaluadas += mio.conPlazo;
                        aTiempo += mio.aTiempo;
                        esperadoAcumulado += tasaOtros * mio.conPlazo;
                        tareasPorNivel.merge(c.nivel(), mio.conPlazo, Integer::sum);
                        break;
                    }
                }
            }

            if (mio.conEjecucion > 0) {
                for (Comparacion c : comparaciones) {
                    int ejecucionOtros = c.flota().conEjecucion - c.propio().conEjecucion;
                    if (ejecucionOtros >= MIN_TAREAS_FLOTA) {
                        double promedioOtros = (double) (c.flota().minutos - c.propio().minutos) / ejecucionOtros;
                        conEjecucion += mio.conEjecucion;
                        minutosEsperados += promedioOtros * mio.conEjecucion;
                        minutosReales += mio.minutos;
                        break;
                    }
                }
            }
        }

        Double real = null;
        Double esperado = null;
        Double diferencia = null;
        Double ajustado = null;
        Double porcentajeGeneral = general.porcentajeATiempo();
        if (evaluadas >= MIN_TAREAS_MENSAJERO) {
            real = aTiempo * 100.0 / evaluadas;
            esperado = esperadoAcumulado * 100.0 / evaluadas;
            diferencia = real - esperado;
            // Se lleva a la escala de TODA la flota, no de su area: asi un
            // tecnico promedio y un mensajero promedio sacan la misma nota y
            // los cortes 85/60 significan lo mismo en los dos rankings.
            if (porcentajeGeneral != null) {
                ajustado = Math.max(0.0, Math.min(100.0, porcentajeGeneral + diferencia));
            }
        }

        Double eficiencia = null;
        // Con 0 minutos reales (cierres instantaneos) el cociente no dice nada
        if (conEjecucion >= MIN_TAREAS_MENSAJERO && minutosReales > 0) {
            eficiencia = minutosEsperados / minutosReales;
        }

        return new Evaluacion(area, evaluadas, redondear(real), redondear(esperado), redondear(diferencia),
                redondear(ajustado), redondear(eficiencia), tareasPorVehiculo, tareasPorTipo, tareasPorNivel);
    }

    private record Comparacion(Nivel nivel, Acumulado flota, Acumulado propio) {
    }

    private static Double redondear(Double valor) {
        return valor != null ? Math.round(valor * 100.0) / 100.0 : null;
    }
}
