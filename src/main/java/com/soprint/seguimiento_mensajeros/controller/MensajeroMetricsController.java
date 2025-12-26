package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.MensajeroMetricsDTO;
import com.soprint.seguimiento_mensajeros.service.MensajeroMetricsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para endpoints de métricas de mensajeros.
 */
@RestController
@RequestMapping("/api/metricas/mensajeros")
public class MensajeroMetricsController {

    private final MensajeroMetricsService metricsService;

    public MensajeroMetricsController(MensajeroMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Obtiene las métricas de un mensajero específico.
     * 
     * Ejemplo: GET
     * /api/metricas/mensajeros/1?fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/{idMensajero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<MensajeroMetricsDTO> getMetricasMensajero(
            @PathVariable Long idMensajero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            MensajeroMetricsDTO metricas = metricsService.getMetricasMensajero(idMensajero, fechaInicio, fechaFin);
            return ResponseEntity.ok(metricas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene el resumen diario de un mensajero.
     * 
     * Ejemplo: GET /api/metricas/mensajeros/1/dia/2024-01-15
     */
    @GetMapping("/{idMensajero}/dia/{fecha}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'MENSAJERO')")
    public ResponseEntity<MensajeroMetricsDTO> getResumenDiario(
            @PathVariable Long idMensajero,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            MensajeroMetricsDTO metricas = metricsService.getResumenDiario(idMensajero, fecha);
            return ResponseEntity.ok(metricas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene un comparativo de todos los mensajeros ordenados por rendimiento.
     * 
     * Ejemplo: GET
     * /api/metricas/mensajeros/comparativo?fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/comparativo")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<MensajeroMetricsDTO>> getComparativoMensajeros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<MensajeroMetricsDTO> metricas = metricsService.getComparativoMensajeros(fechaInicio, fechaFin);
        return ResponseEntity.ok(metricas);
    }
}
