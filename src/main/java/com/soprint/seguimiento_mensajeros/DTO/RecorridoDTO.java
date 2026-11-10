package com.soprint.seguimiento_mensajeros.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recorrido de un mensajero en un día: los puntos GPS que mandó la app, las tareas que
 * hizo, dónde se detuvo y un resumen. Lo arma RecorridoService a partir de
 * posicion_tarea_historico.
 */
public record RecorridoDTO(
        Long idMensajero,
        String nombreMensajero,
        LocalDate fecha,
        List<Punto> puntos,
        List<TareaRecorrido> tareas,
        List<Parada> paradas,
        Resumen resumen) {

    /**
     * Un punto del trayecto. {@code corte} = true indica que antes de este punto hubo un
     * hueco (sin señal o la app no mandó posición) y el mensajero apareció lejos: el tramo
     * hasta aquí no es el camino real y se dibuja distinto.
     */
    public record Punto(double lat, double lng, LocalDateTime hora, Long idTarea, boolean corte) {
    }

    /** Tarea del día con la ubicación del cliente, para marcarla en el mapa. */
    public record TareaRecorrido(
            Long idTarea,
            String codigo,
            String nombre,
            String tipoOperacion,
            String cliente,
            Double clienteLat,
            Double clienteLng,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
    }

    /** Lugar donde estuvo quieto al menos el mínimo configurado. */
    public record Parada(double lat, double lng, LocalDateTime inicio, LocalDateTime fin, long minutos,
            Long idTarea) {
    }

    public record Resumen(
            int totalPuntos,
            double km,
            LocalDateTime primeraPosicion,
            LocalDateTime ultimaPosicion,
            long minutosJornada,
            long minutosParado,
            long minutosMovimiento,
            int cortes) {
    }
}
