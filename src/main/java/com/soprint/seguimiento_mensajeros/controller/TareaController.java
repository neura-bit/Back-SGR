package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.TareaResponse;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.service.ITareaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final ITareaService tareaService;
    private final UsuarioRepository usuarioRepository;

    public TareaController(ITareaService tareaService, UsuarioRepository usuarioRepository) {
        this.tareaService = tareaService;
        this.usuarioRepository = usuarioRepository;
    }

    // GET /api/tareas
    @GetMapping
    public ResponseEntity<List<TareaResponse>> listar() {
        List<TareaResponse> tareas = tareaService.findAll().stream()
                .map(TareaResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tareas);
    }

    // GET /api/tareas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> obtenerPorId(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(tarea -> ResponseEntity.ok(TareaResponse.fromEntity(tarea)))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/tareas
    @PostMapping
    public ResponseEntity<Tarea> crear(@RequestBody Tarea tarea) {
        Tarea creada = tareaService.create(tarea);
        return ResponseEntity
                .created(URI.create("/api/tareas/" + creada.getIdTarea()))
                .body(creada);
    }

    // PUT /api/tareas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(@PathVariable Long id,
            @RequestBody Tarea tarea) {
        try {
            Tarea actualizada = tareaService.update(id, tarea);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/tareas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            tareaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== ROLE-SPECIFIC ENDPOINTS =====

    // GET /api/tareas/mis-tareas - MENSAJERO gets their assigned tasks (only
    // active: CREADA, PENDIENTE, EN PROCESO)
    @GetMapping("/mis-tareas")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<List<Tarea>> misTareas(Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        // Solo devuelve tareas con estado CREADA, PENDIENTE o EN PROCESO
        return ResponseEntity.ok(tareaService.findTareasActivasByMensajero(usuario.getIdUsuario()));
    }

    // PUT /api/tareas/{id}/asignar - ASESOR assigns a task to a messenger
    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
    public ResponseEntity<Tarea> asignarMensajero(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long idMensajero = body.get("idMensajero");
            if (idMensajero == null) {
                return ResponseEntity.badRequest().build();
            }
            Tarea actualizada = tareaService.asignarMensajero(id, idMensajero);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/tareas/{id}/reasignar-mensajero - SUPERVISOR reassigns a task to a
    // different messenger
    @PutMapping("/{id}/reasignar-mensajero")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<Tarea> reasignarMensajero(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long idMensajero = body.get("idMensajero");
            if (idMensajero == null) {
                return ResponseEntity.badRequest().build();
            }
            Tarea actualizada = tareaService.reasignarMensajero(id, idMensajero);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/tareas/{id}/iniciar - MENSAJERO inicia una tarea (cambia estado a EN
    // PROCESO y registra fecha inicio)
    @PutMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<?> iniciarTarea(@PathVariable Long id) {
        try {
            Tarea tareaIniciada = tareaService.iniciarTarea(id);
            return ResponseEntity.ok(tareaIniciada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/tareas/{id}/finalizar - MENSAJERO finaliza una tarea
    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<?> finalizarTarea(
            @PathVariable Long id,
            @RequestBody com.soprint.seguimiento_mensajeros.DTO.FinalizarTareaRequest request) {
        try {
            // Validar campos requeridos
            if (request.getCodigo() == null || request.getCodigo().isBlank()) {
                return ResponseEntity.badRequest().body("El código es requerido");
            }
            if (request.getIdEstadoTarea() == null) {
                return ResponseEntity.badRequest().body("El estado de la tarea es requerido");
            }

            Tarea tareaFinalizada = tareaService.finalizarTarea(
                    id,
                    request.getCodigo(),
                    request.getIdEstadoTarea(),
                    request.getObservacion());
            return ResponseEntity.ok(tareaFinalizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/tareas/{id}/finalizar-sin-codigo - Finaliza una tarea sin validar
    // código
    @PutMapping("/{id}/finalizar-sin-codigo")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<?> finalizarTareaSinCodigo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long idEstadoTarea = body.get("idEstadoTarea") != null
                    ? ((Number) body.get("idEstadoTarea")).longValue()
                    : null;
            String observacion = (String) body.get("observacion");

            if (idEstadoTarea == null) {
                return ResponseEntity.badRequest().body("El estado de la tarea es requerido");
            }

            Tarea tareaFinalizada = tareaService.finalizarTareaSinCodigo(id, idEstadoTarea, observacion);
            return ResponseEntity.ok(tareaFinalizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/tareas/{id}/reenviar-codigo - Reenvía código y datos del cliente al
    // webhook
    @PostMapping("/{id}/reenviar-codigo")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN', 'ASESOR')")
    public ResponseEntity<?> reenviarCodigoTarea(@PathVariable Long id) {
        try {
            tareaService.reenviarCodigoTarea(id);
            return ResponseEntity.ok().body(java.util.Map.of("mensaje", "Código reenviado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/tareas/por-fechas - Obtener tareas por rango de fechas
    // Ejemplo:
    // /api/tareas/por-fechas?fechaInicio=2024-01-01T00:00:00&fechaFin=2024-01-31T23:59:59
    @GetMapping("/por-fechas")
    public ResponseEntity<List<TareaResponse>> obtenerPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

            List<TareaResponse> tareas = tareaService.findByRangoFechas(inicio, fin).stream()
                    .map(TareaResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tareas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/tareas/mis-tareas-completadas - Obtener tareas COMPLETADAS del
    // mensajero por rango de fechas
    // Ejemplo:
    // /api/tareas/mis-tareas-completadas?fechaInicio=2024-01-01T00:00:00&fechaFin=2024-01-31T23:59:59
    @GetMapping("/mis-tareas-completadas")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<List<TareaResponse>> misTareasCompletadas(
            Authentication authentication,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);

            List<TareaResponse> tareas = tareaService.findTareasCompletadasByMensajeroAndFechas(
                    usuario.getIdUsuario(), inicio, fin).stream()
                    .map(TareaResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tareas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
