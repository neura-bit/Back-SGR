package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjunto;
import com.soprint.seguimiento_mensajeros.service.ArchivoAdjuntoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ArchivoAdjuntoController {

    private final ArchivoAdjuntoService archivoAdjuntoService;

    public ArchivoAdjuntoController(ArchivoAdjuntoService archivoAdjuntoService) {
        this.archivoAdjuntoService = archivoAdjuntoService;
    }

    /**
     * POST /api/tareas/{idTarea}/archivos - Subir archivo a una tarea
     */
    @PostMapping("/tareas/{idTarea}/archivos")
    public ResponseEntity<?> subirArchivo(
            @PathVariable Long idTarea,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            ArchivoAdjunto guardado = archivoAdjuntoService.subirArchivo(archivo, idTarea);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Archivo subido exitosamente",
                    "idArchivo", guardado.getIdArchivo(),
                    "nombreOriginal", guardado.getNombreOriginal(),
                    "tipoMime", guardado.getTipoMime(),
                    "tamanioBytes", guardado.getTamanioBytes()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al subir el archivo: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tareas/{idTarea}/archivos - Listar archivos de una tarea
     */
    @GetMapping("/tareas/{idTarea}/archivos")
    public ResponseEntity<List<ArchivoAdjunto>> listarArchivos(@PathVariable Long idTarea) {
        List<ArchivoAdjunto> archivos = archivoAdjuntoService.listarArchivos(idTarea);
        return ResponseEntity.ok(archivos);
    }

    /**
     * GET /api/archivos/{idArchivo} - Descargar un archivo
     */
    @GetMapping("/archivos/{idArchivo}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long idArchivo) {
        try {
            ArchivoAdjunto archivo = archivoAdjuntoService.obtenerArchivo(idArchivo);
            Resource resource = archivoAdjuntoService.descargarArchivo(idArchivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/archivos/{idArchivo}/info - Obtener información de un archivo
     */
    @GetMapping("/archivos/{idArchivo}/info")
    public ResponseEntity<?> obtenerInfoArchivo(@PathVariable Long idArchivo) {
        try {
            ArchivoAdjunto archivo = archivoAdjuntoService.obtenerArchivo(idArchivo);
            return ResponseEntity.ok(Map.of(
                    "idArchivo", archivo.getIdArchivo(),
                    "nombreOriginal", archivo.getNombreOriginal(),
                    "tipoMime", archivo.getTipoMime(),
                    "tamanioBytes", archivo.getTamanioBytes(),
                    "fechaSubida", archivo.getFechaSubida().toString(),
                    "idTarea", archivo.getTarea().getIdTarea()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/archivos/{idArchivo} - Eliminar un archivo
     */
    @DeleteMapping("/archivos/{idArchivo}")
    public ResponseEntity<?> eliminarArchivo(@PathVariable Long idArchivo) {
        try {
            archivoAdjuntoService.eliminarArchivo(idArchivo);
            return ResponseEntity.ok(Map.of("mensaje", "Archivo eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al eliminar el archivo: " + e.getMessage()));
        }
    }
}
