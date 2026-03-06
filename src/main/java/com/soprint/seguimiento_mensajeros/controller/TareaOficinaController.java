package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.TareaOficina;
import com.soprint.seguimiento_mensajeros.service.ITareaOficinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oficina/tareas")
@CrossOrigin(origins = "*") // Para que Flutter y React puedan acceder
public class TareaOficinaController {

    @Autowired
    private ITareaOficinaService tareaOficinaService;

    @GetMapping
    public ResponseEntity<List<TareaOficina>> getAll() {
        return ResponseEntity.ok(tareaOficinaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaOficina> getById(@PathVariable Long id) {
        return tareaOficinaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TareaOficina> create(@RequestBody TareaOficina tarea) {
        TareaOficina nuevaTarea = tareaOficinaService.create(tarea);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTarea);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaOficina> update(@PathVariable Long id, @RequestBody TareaOficina tarea) {
        try {
            return ResponseEntity.ok(tareaOficinaService.update(id, tarea));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tareaOficinaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para obtener tareas por responsable (con o sin paginación)
    @GetMapping("/responsable/{idResponsable}")
    public ResponseEntity<?> getByResponsable(
            @PathVariable Long idResponsable,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            Page<TareaOficina> tareasPage = tareaOficinaService.findByResponsable(idResponsable, pageable);
            return ResponseEntity.ok(tareasPage);
        } else {
            List<TareaOficina> tareasList = tareaOficinaService.findByResponsable(idResponsable);
            return ResponseEntity.ok(tareasList);
        }
    }

    // Endpoint para obtener tareas creadas por un usuario
    @GetMapping("/creador/{idCreador}")
    public ResponseEntity<?> getByCreador(
            @PathVariable Long idCreador,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            Page<TareaOficina> tareasPage = tareaOficinaService.findByCreador(idCreador, pageable);
            return ResponseEntity.ok(tareasPage);
        } else {
            List<TareaOficina> tareasList = tareaOficinaService.findByCreador(idCreador);
            return ResponseEntity.ok(tareasList);
        }
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<TareaOficina> completarTarea(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tareaOficinaService.completarTarea(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
