package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoVehiculo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluacion de mensajeros ajustada por el vehiculo con el que hicieron cada
 * tarea.
 *
 * Llegar a tiempo en camion no cuesta lo mismo que en moto, asi que comparar
 * a todos contra el mismo porcentaje castiga al que maneja el vehiculo mas
 * lento. Aca cada tarea se compara contra lo que logro el resto de la flota
 * con ese mismo vehiculo, en el mismo periodo y el mismo alcance.
 *
 * Se descarto a proposito usar factores fijos ("la moto es 1.4 veces mas
 * rapida"): la referencia sale de los datos reales, se recalcula en cada
 * consulta y se expone para que se pueda auditar.
 *
 * La referencia de cada mensajero excluye sus propias tareas. Si no, el
 * unico mensajero en camion de una sucursal seria su propia referencia y
 * siempre quedaria "igual que sus pares".
 *
 * No depende de Spring ni de la base: recibe registros y devuelve numeros,
 * para poder probarla sola.
 */
public final class BaselineVehiculos {

    /** Tareas de otros mensajeros con ese vehiculo para confiar en su promedio. */
    public static final int MIN_TAREAS_FLOTA = 20;

    /** Tareas del propio mensajero con referencia valida para darle un indice. */
    public static final int MIN_TAREAS_MENSAJERO = 5;

    /** Una tarea finalizada con vehiculo declarado. */
    public record Registro(Long idMensajero, TipoVehiculo vehiculo, Boolean aTiempo, Long minutosEjecucion) {
    }

    /** Lo que logro toda la flota del alcance con un vehiculo. */
    public record Referencia(TipoVehiculo vehiculo, int tareas, int mensajeros, Double porcentajeATiempo,
            Double minutosEjecucionPromedio, boolean suficiente) {
    }

    /**
     * Resultado por mensajero. Los campos quedan en null cuando no hay base
     * suficiente: preferimos "sin base" a un numero que no significa nada.
     */
    public record Evaluacion(
            /** Tareas del mensajero que entraron al calculo del cumplimiento. */
            int tareasEvaluadas,
            /** % a tiempo del mensajero sobre esas mismas tareas. */
            Double cumplimientoReal,
            /** % que habria logrado un mensajero promedio con su mezcla de vehiculos. */
            Double cumplimientoEsperado,
            /** real - esperado, en puntos porcentuales. */
            Double diferenciaVsPares,
            /** Cumplimiento llevado a la escala de la flota: general + diferencia. */
            Double cumplimientoAjustado,
            /** Tiempo de ejecucion esperado / real. 1.00 = ritmo de sus pares. */
            Double indiceEficiencia,
            /** Cuantas tareas hizo con cada vehiculo (todas las declaradas). */
            Map<TipoVehiculo, Integer> tareasPorVehiculo) {
    }

    /** Acumulador de una combinacion (vehiculo) o (mensajero, vehiculo). */
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
    }

    private final Map<TipoVehiculo, Acumulado> flota = new EnumMap<>(TipoVehiculo.class);
    private final Map<Long, Map<TipoVehiculo, Acumulado>> porMensajero = new LinkedHashMap<>();
    private final Double porcentajeGeneral;

    public BaselineVehiculos(List<Registro> registros) {
        int conPlazo = 0;
        int aTiempo = 0;
        for (Registro r : registros) {
            if (r.vehiculo() == null || r.idMensajero() == null) {
                continue;
            }
            flota.computeIfAbsent(r.vehiculo(), v -> new Acumulado()).sumar(r);
            porMensajero.computeIfAbsent(r.idMensajero(), id -> new EnumMap<>(TipoVehiculo.class))
                    .computeIfAbsent(r.vehiculo(), v -> new Acumulado()).sumar(r);
            if (r.aTiempo() != null) {
                conPlazo++;
                if (r.aTiempo()) {
                    aTiempo++;
                }
            }
        }
        this.porcentajeGeneral = conPlazo > 0 ? aTiempo * 100.0 / conPlazo : null;
    }

    /** % a tiempo de toda la flota del alcance, sin distinguir vehiculo. */
    public Double getPorcentajeGeneral() {
        return redondear(porcentajeGeneral);
    }

    /** La tabla de referencia que se muestra en pantalla y en el PDF. */
    public List<Referencia> referencias() {
        List<Referencia> lista = new ArrayList<>();
        for (Map.Entry<TipoVehiculo, Acumulado> e : flota.entrySet()) {
            Acumulado a = e.getValue();
            int mensajeros = (int) porMensajero.values().stream()
                    .filter(m -> m.containsKey(e.getKey()))
                    .count();
            lista.add(new Referencia(
                    e.getKey(),
                    a.tareas,
                    mensajeros,
                    a.conPlazo > 0 ? redondear(a.aTiempo * 100.0 / a.conPlazo) : null,
                    a.conEjecucion > 0 ? redondear((double) a.minutos / a.conEjecucion) : null,
                    a.conPlazo >= MIN_TAREAS_FLOTA));
        }
        return lista;
    }

    public Evaluacion evaluar(Long idMensajero) {
        Map<TipoVehiculo, Acumulado> propias = porMensajero.getOrDefault(idMensajero, Map.of());

        Map<TipoVehiculo, Integer> tareasPorVehiculo = new EnumMap<>(TipoVehiculo.class);
        propias.forEach((v, a) -> tareasPorVehiculo.put(v, a.tareas));

        // Cumplimiento: cada tarea con plazo se compara contra el % a tiempo
        // del resto de la flota con ese vehiculo.
        int evaluadas = 0;
        int aTiempo = 0;
        double esperadoAcumulado = 0;

        // Eficiencia: minutos que le habrian tomado a sus pares frente a los reales.
        int conEjecucion = 0;
        double minutosEsperados = 0;
        long minutosReales = 0;

        for (Map.Entry<TipoVehiculo, Acumulado> e : propias.entrySet()) {
            Acumulado mio = e.getValue();
            Acumulado total = flota.get(e.getKey());

            int plazoOtros = total.conPlazo - mio.conPlazo;
            if (mio.conPlazo > 0 && plazoOtros >= MIN_TAREAS_FLOTA) {
                double tasaOtros = (double) (total.aTiempo - mio.aTiempo) / plazoOtros;
                evaluadas += mio.conPlazo;
                aTiempo += mio.aTiempo;
                esperadoAcumulado += tasaOtros * mio.conPlazo;
            }

            int ejecucionOtros = total.conEjecucion - mio.conEjecucion;
            if (mio.conEjecucion > 0 && ejecucionOtros >= MIN_TAREAS_FLOTA) {
                double promedioOtros = (double) (total.minutos - mio.minutos) / ejecucionOtros;
                conEjecucion += mio.conEjecucion;
                minutosEsperados += promedioOtros * mio.conEjecucion;
                minutosReales += mio.minutos;
            }
        }

        Double real = null;
        Double esperado = null;
        Double diferencia = null;
        Double ajustado = null;
        if (evaluadas >= MIN_TAREAS_MENSAJERO) {
            real = aTiempo * 100.0 / evaluadas;
            esperado = esperadoAcumulado * 100.0 / evaluadas;
            diferencia = real - esperado;
            if (porcentajeGeneral != null) {
                ajustado = Math.max(0.0, Math.min(100.0, porcentajeGeneral + diferencia));
            }
        }

        Double eficiencia = null;
        // Con 0 minutos reales (cierres instantaneos) el cociente no dice nada
        if (conEjecucion >= MIN_TAREAS_MENSAJERO && minutosReales > 0) {
            eficiencia = minutosEsperados / minutosReales;
        }

        return new Evaluacion(evaluadas, redondear(real), redondear(esperado), redondear(diferencia),
                redondear(ajustado), redondear(eficiencia), tareasPorVehiculo);
    }

    private static Double redondear(Double valor) {
        return valor != null ? Math.round(valor * 100.0) / 100.0 : null;
    }
}
