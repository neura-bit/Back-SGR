package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.Sucursal;
import com.soprint.seguimiento_mensajeros.service.ISucursalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final ISucursalService sucursalService;

    public SucursalController(ISucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    public ResponseEntity<List<Sucursal>> listar() {
        return ResponseEntity.ok(sucursalService.findAll());
    }

    /**
     * GET /api/sucursales/ciudades
     *
     * Solo los nombres de ciudad, para el desplegable del formulario de
     * cliente. Va aparte del listado completo de sucursales porque ese está
     * restringido a ADMIN y este lo necesitan también ASESOR y SUPERVISOR,
     * que son quienes registran clientes. No expone direcciones ni teléfonos.
     */
    @GetMapping("/ciudades")
    public ResponseEntity<List<String>> listarCiudades() {
        return ResponseEntity.ok(sucursalService.findCiudades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerPorId(@PathVariable Long id) {
        return sucursalService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Sucursal> crear(@RequestBody Sucursal sucursal) {
        Sucursal creada = sucursalService.create(sucursal);
        return ResponseEntity
                .created(URI.create("/api/sucursales/" + creada.getIdSucursal()))
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Long id, @RequestBody Sucursal sucursal) {
        try {
            Sucursal actualizada = sucursalService.update(id, sucursal);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            sucursalService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
