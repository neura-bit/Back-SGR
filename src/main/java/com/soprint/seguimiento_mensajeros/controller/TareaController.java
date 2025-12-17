package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.service.ITareaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<Tarea>> listar() {
        return ResponseEntity.ok(tareaService.findAll());
    }

    // GET /api/tareas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerPorId(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(ResponseEntity::ok)
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

    // GET /api/tareas/mis-tareas - MENSAJERO gets their assigned tasks
    @GetMapping("/mis-tareas")
    @PreAuthorize("hasAnyRole('MENSAJERO', 'ADMIN')")
    public ResponseEntity<List<Tarea>> misTareas(Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tareaService.findByMensajero(usuario.getIdUsuario()));
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
}
