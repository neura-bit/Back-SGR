package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.EstadoTarea;
import com.soprint.seguimiento_mensajeros.service.IEstadoTareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estado-tareas")
public class EstadoTareaController {

    private final IEstadoTareaService estadoTareaService;

    public EstadoTareaController(IEstadoTareaService estadoTareaService) {
        this.estadoTareaService = estadoTareaService;
    }

    @GetMapping
    public ResponseEntity<List<EstadoTarea>> listar() {
        return ResponseEntity.ok(estadoTareaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadoTarea> obtenerPorId(@PathVariable Long id) {
        return estadoTareaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EstadoTarea> crear(@Valid @RequestBody EstadoTarea estadoTarea) {
        EstadoTarea creado = estadoTareaService.create(estadoTarea);
        return ResponseEntity
                .created(URI.create("/api/estado-tareas/" + creado.getIdEstadoTarea()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadoTarea> actualizar(@PathVariable Long id,
                                                  @Valid @RequestBody EstadoTarea estadoTarea) {
        try {
            EstadoTarea actualizado = estadoTareaService.update(id, estadoTarea);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            estadoTareaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
