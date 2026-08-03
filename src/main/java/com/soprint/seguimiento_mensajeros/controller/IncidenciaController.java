package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaRequest;
import com.soprint.seguimiento_mensajeros.DTO.IncidenciaResponse;
import com.soprint.seguimiento_mensajeros.model.Incidencia;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.service.IIncidenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IIncidenciaService incidenciaService;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaController(IIncidenciaService incidenciaService, UsuarioRepository usuarioRepository) {
        this.incidenciaService = incidenciaService;
        this.usuarioRepository = usuarioRepository;
    }

    // POST /api/incidencias - el mensajero reporta al finalizar la tarea
    @PostMapping
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<?> reportar(@Valid @RequestBody IncidenciaRequest request,
                                      Authentication authentication) {
        try {
            Usuario mensajero = authentication != null
                    ? usuarioRepository.findByUsername(authentication.getName()).orElse(null)
                    : null;

            Incidencia incidencia = incidenciaService.registrar(request, mensajero);
            return ResponseEntity.ok(IncidenciaResponse.fromEntity(incidencia));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // GET /api/incidencias?fechaInicio=...&fechaFin=... - solo administradores
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidenciaResponse>> listarPorFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

            List<IncidenciaResponse> incidencias = incidenciaService.findByRangoFechas(inicio, fin).stream()
                    .map(IncidenciaResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(incidencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
