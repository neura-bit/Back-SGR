package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Parada;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Punto;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO.Resumen;
import com.soprint.seguimiento_mensajeros.repository.PosicionTareaHistoricoRepository.PuntoRecorrido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casos sacados de lo que se vio en posicion_tarea_historico de producción (nov. 2026):
 * ráfagas de puntos con la misma hora, saltos del GPS, huecos sin señal y vibración quieto.
 */
class RecorridoServiceTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 9, 9, 0);
    /** ~0,0009° de latitud son ~100 m. */
    private static final double CIEN_M = 0.0009;

    private static PuntoRecorrido p(double lat, double lng, int segundos) {
        return new PuntoRecorrido(lat, lng, T0.plusSeconds(segundos), null);
    }

    private static PuntoRecorrido p(double lat, double lng, LocalDateTime hora) {
        return new PuntoRecorrido(lat, lng, hora, null);
    }

    @Test
    @DisplayName("Un salto del GPS de 2 km en 30 s se descarta")
    void descartaSaltoImposible() {
        List<Punto> r = RecorridoService.limpiarYMarcarCortes(List.of(
                p(-0.20, -78.50, 0),
                p(-0.20 + 20 * CIEN_M, -78.50, 30),
                p(-0.20 + CIEN_M, -78.50, 60)));

        assertEquals(2, r.size());
        assertEquals(-0.20 + CIEN_M, r.get(1).lat(), 1e-9);
    }

    @Test
    @DisplayName("Si el 'salto' se confirma con varios puntos seguidos, se acepta")
    void aceptaSaltoConfirmado() {
        List<PuntoRecorrido> crudos = new ArrayList<>();
        crudos.add(p(-0.20, -78.50, 0));
        for (int k = 1; k <= 5; k++) {
            crudos.add(p(-0.20 + 20 * CIEN_M, -78.50, k * 30));
        }
        List<Punto> r = RecorridoService.limpiarYMarcarCortes(crudos);

        assertTrue(r.size() >= 2, "el mensajero sí estaba en el lugar nuevo");
        assertEquals(-0.20 + 20 * CIEN_M, r.get(r.size() - 1).lat(), 1e-9);
    }

    @Test
    @DisplayName("Ráfaga: puntos con la misma hora y en desorden se ordenan y se reparten en el tiempo")
    void desagrupaRafaga() {
        LocalDateTime llegada = T0.plusMinutes(4);
        List<PuntoRecorrido> crudos = List.of(
                p(-0.20, -78.50, 0),
                // llegan juntos, desordenados: 300 m, 100 m, 200 m
                p(-0.20 + 3 * CIEN_M, -78.50, llegada),
                p(-0.20 + CIEN_M, -78.50, llegada.plusNanos(3_000_000)),
                p(-0.20 + 2 * CIEN_M, -78.50, llegada.plusNanos(5_000_000)));

        List<PuntoRecorrido> r = RecorridoService.desagruparRafagas(crudos);

        assertEquals(4, r.size());
        assertEquals(-0.20 + CIEN_M, r.get(1).latitud(), 1e-9);
        assertEquals(-0.20 + 2 * CIEN_M, r.get(2).latitud(), 1e-9);
        assertEquals(-0.20 + 3 * CIEN_M, r.get(3).latitud(), 1e-9);
        assertTrue(r.get(1).fechaRegistro().isBefore(r.get(2).fechaRegistro()));
        assertTrue(r.get(2).fechaRegistro().isBefore(r.get(3).fechaRegistro()));

        // y ya no se descartan como saltos
        assertEquals(4, RecorridoService.limpiarYMarcarCortes(crudos).size());
    }

    @Test
    @DisplayName("Hueco largo y lejos se marca como corte")
    void marcaCorte() {
        List<Punto> r = RecorridoService.limpiarYMarcarCortes(List.of(
                p(-0.20, -78.50, 0),
                p(-0.20 + 30 * CIEN_M, -78.50, 20 * 60)));

        assertFalse(r.get(0).corte());
        assertTrue(r.get(1).corte());
    }

    @Test
    @DisplayName("Quieto 10 minutos con vibración del GPS es una parada y no suma km")
    void paradaSinKm() {
        List<PuntoRecorrido> crudos = new ArrayList<>();
        for (int k = 0; k <= 20; k++) {
            double ruido = (k % 2 == 0 ? 1 : -1) * 0.0001; // ~11 m de vibración
            crudos.add(p(-0.20 + ruido, -78.50, k * 30));
        }
        List<Punto> puntos = RecorridoService.limpiarYMarcarCortes(crudos);
        List<Parada> paradas = RecorridoService.detectarParadas(puntos);
        Resumen resumen = RecorridoService.resumir(puntos, paradas);

        assertEquals(1, paradas.size());
        assertEquals(10, paradas.get(0).minutos());
        assertEquals(0.0, resumen.km());
        assertEquals(10, resumen.minutosParado());
        assertEquals(0, resumen.minutosMovimiento());
    }

    @Test
    @DisplayName("Un kilómetro en línea recta suma 1 km")
    void sumaKm() {
        List<PuntoRecorrido> crudos = new ArrayList<>();
        for (int k = 0; k <= 10; k++) {
            crudos.add(p(-0.20 + k * CIEN_M, -78.50, k * 20));
        }
        List<Punto> puntos = RecorridoService.limpiarYMarcarCortes(crudos);
        Resumen resumen = RecorridoService.resumir(puntos, RecorridoService.detectarParadas(puntos));

        assertEquals(1.0, resumen.km(), 0.05);
    }

    @Test
    @DisplayName("Sin posiciones devuelve un resumen vacío")
    void sinPuntos() {
        Resumen r = RecorridoService.resumir(List.of(), List.of());
        assertEquals(0, r.totalPuntos());
        assertNull(r.primeraPosicion());
    }
}
