package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.IncidenciaTareaRequest;
import com.soprint.seguimiento_mensajeros.DTO.IncidenciaTareaResponse;
import com.soprint.seguimiento_mensajeros.model.IncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.MotivoIncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.service.IIncidenciaTareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidencias-tarea")
public class IncidenciaTareaController {

    private final IIncidenciaTareaService incidenciaTareaService;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaTareaController(IIncidenciaTareaService incidenciaTareaService,
                                     UsuarioRepository usuarioRepository) {
        this.incidenciaTareaService = incidenciaTareaService;
        this.usuarioRepository = usuarioRepository;
    }

    // POST /api/incidencias-tarea - el supervisor reporta al armar la ruta
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<?> reportar(@Valid @RequestBody IncidenciaTareaRequest request,
                                      Authentication authentication) {
        try {
            IncidenciaTarea incidencia = incidenciaTareaService.registrar(request, usuarioAutenticado(authentication));
            return ResponseEntity.ok(IncidenciaTareaResponse.fromEntity(incidencia));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * GET /api/incidencias-tarea/mis-novedades - lo que alimenta el panel del
     * asesor. Devuelve solo las abiertas de sus tareas todavía vivas: la alerta
     * se apaga cuando la tarea termina, aunque la fila siga guardada.
     */
    @GetMapping("/mis-novedades")
    @PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
    public ResponseEntity<?> misNovedades(Authentication authentication) {
        Usuario asesor = usuarioAutenticado(authentication);
        if (asesor == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "No se pudo identificar al usuario"));
        }

        List<IncidenciaTareaResponse> novedades = incidenciaTareaService
                .findVigentesPorAsesor(asesor.getIdUsuario()).stream()
                .map(IncidenciaTareaResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(novedades);
    }

    /**
     * GET /api/incidencias-tarea/motivos - catálogo para el selector del
     * supervisor. Sale del enum en vez de estar escrito en la web, así al
     * agregar un motivo nuevo aparece solo, sin tocar el frontend.
     */
    @GetMapping("/motivos")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'ASESOR')")
    public ResponseEntity<List<Map<String, String>>> motivos() {
        return ResponseEntity.ok(Arrays.stream(MotivoIncidenciaTarea.values())
                .map(m -> Map.of("valor", m.name(), "etiqueta", m.getEtiqueta()))
                .collect(Collectors.toList()));
    }

    // GET /api/incidencias-tarea/tarea/{idTarea} - las de una tarea puntual
    @GetMapping("/tarea/{idTarea}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'ASESOR')")
    public ResponseEntity<List<IncidenciaTareaResponse>> porTarea(@PathVariable Long idTarea) {
        return ResponseEntity.ok(incidenciaTareaService.findByTarea(idTarea).stream()
                .map(IncidenciaTareaResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    // GET /api/incidencias-tarea?fechaInicio=...&fechaFin=... - histórico
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidenciaTareaResponse>> listarPorFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

            return ResponseEntity.ok(incidenciaTareaService.findByRangoFechas(inicio, fin).stream()
                    .map(IncidenciaTareaResponse::fromEntity)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/incidencias-tarea/{id}/resolver
     *
     * Sin botón en la web todavía: hoy la novedad se apaga sola cuando la tarea
     * termina. Queda disponible para cuando se quiera cerrarla antes.
     */
    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<?> resolver(@PathVariable Long id, Authentication authentication) {
        try {
            IncidenciaTarea incidencia = incidenciaTareaService.resolver(id, usuarioAutenticado(authentication));
            return ResponseEntity.ok(IncidenciaTareaResponse.fromEntity(incidencia));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Usuario usuarioAutenticado(Authentication authentication) {
        return authentication != null
                ? usuarioRepository.findByUsername(authentication.getName()).orElse(null)
                : null;
    }
}
