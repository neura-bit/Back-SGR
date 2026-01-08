package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.PosicionMensajeroResponse;
import com.soprint.seguimiento_mensajeros.DTO.PosicionRequest;
import com.soprint.seguimiento_mensajeros.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
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
}
