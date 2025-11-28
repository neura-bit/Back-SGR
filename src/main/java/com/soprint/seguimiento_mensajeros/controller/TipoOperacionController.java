package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.TipoOperacion;
import com.soprint.seguimiento_mensajeros.service.ITipoOperacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tipo-operaciones")
public class TipoOperacionController {

    private final ITipoOperacionService tipoOperacionService;

    public TipoOperacionController(ITipoOperacionService tipoOperacionService) {
        this.tipoOperacionService = tipoOperacionService;
    }

    @GetMapping
    public ResponseEntity<List<TipoOperacion>> listar() {
        return ResponseEntity.ok(tipoOperacionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoOperacion> obtenerPorId(@PathVariable Long id) {
        return tipoOperacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TipoOperacion> crear(@RequestBody TipoOperacion tipoOperacion) {
        TipoOperacion creado = tipoOperacionService.create(tipoOperacion);
        return ResponseEntity
                .created(URI.create("/api/tipo-operaciones/" + creado.getIdTipoOperacion()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoOperacion> actualizar(@PathVariable Long id,
                                                    @RequestBody TipoOperacion tipoOperacion) {
        try {
            TipoOperacion actualizado = tipoOperacionService.update(id, tipoOperacion);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            tipoOperacionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
