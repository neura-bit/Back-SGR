package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.PosicionMensajeroResponse;
import com.soprint.seguimiento_mensajeros.DTO.PosicionRequest;
import com.soprint.seguimiento_mensajeros.DTO.RecorridoDTO;
import com.soprint.seguimiento_mensajeros.service.RecorridoService;
import com.soprint.seguimiento_mensajeros.service.TrackingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;
    private final RecorridoService recorridoService;

    public TrackingController(TrackingService trackingService, RecorridoService recorridoService) {
        this.trackingService = trackingService;
        this.recorridoService = recorridoService;
    }

    /**
     * Endpoint que llama la app móvil del mensajero
     */
    @PostMapping("/posicion")
    public ResponseEntity<Void> registrarPosicion(@RequestBody PosicionRequest request) {
        trackingService.registrarPosicion(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint que usará el dashboard con polling para el mapa
     */
    @GetMapping("/mensajeros-actual")
    public ResponseEntity<List<PosicionMensajeroResponse>> obtenerPosicionesActuales() {
        return ResponseEntity.ok(trackingService.obtenerPosicionesActuales());
    }

    /**
     * Recorrido de un mensajero en un día (pantalla Recorridos). Lo ven el admin y los
     * supervisores con el permiso de Rendimiento, igual que esa sección.
     *
     * Ejemplo: GET /api/tracking/recorrido?idMensajero=12&fecha=2026-11-10
     * y opcionalmente &horaDesde=08:00&horaHasta=13:00
     */
    @GetMapping("/recorrido")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and hasAuthority('VER_RENDIMIENTO'))")
    public ResponseEntity<RecorridoDTO> obtenerRecorrido(
            @RequestParam Long idMensajero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaHasta) {
        try {
            return ResponseEntity.ok(recorridoService.obtenerRecorrido(idMensajero, fecha, horaDesde, horaHasta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
