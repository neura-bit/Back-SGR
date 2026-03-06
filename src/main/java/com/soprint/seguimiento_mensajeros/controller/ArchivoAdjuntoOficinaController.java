package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjuntoOficina;
import com.soprint.seguimiento_mensajeros.service.IArchivoAdjuntoOficinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/oficina/tareas/archivos")
@CrossOrigin(origins = "*")
public class ArchivoAdjuntoOficinaController {

    @Autowired
    private IArchivoAdjuntoOficinaService archivoAdjuntoService;

    @PostMapping("/upload/{idTareaOficina}")
    public ResponseEntity<ArchivoAdjuntoOficina> uploadFile(@RequestParam("file") MultipartFile file,
            @PathVariable Long idTareaOficina) {
        try {
            ArchivoAdjuntoOficina archivo = archivoAdjuntoService.uploadFile(file, idTareaOficina);
            return ResponseEntity.ok(archivo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tarea/{idTareaOficina}")
    public ResponseEntity<List<ArchivoAdjuntoOficina>> getArchivosByTarea(@PathVariable Long idTareaOficina) {
        return ResponseEntity.ok(archivoAdjuntoService.findArchivosByTarea(idTareaOficina));
    }

    @GetMapping("/download/{idArchivo}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long idArchivo) {
        try {
            ArchivoAdjuntoOficina archivo = archivoAdjuntoService.obtenerArchivo(idArchivo);
            Resource resource = archivoAdjuntoService.loadFileAsResource(idArchivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{idArchivo}/info")
    public ResponseEntity<?> obtenerInfoArchivo(@PathVariable Long idArchivo) {
        try {
            ArchivoAdjuntoOficina archivo = archivoAdjuntoService.obtenerArchivo(idArchivo);
            return ResponseEntity.ok(java.util.Map.of(
                    "idArchivo", archivo.getIdArchivo(),
                    "nombreOriginal", archivo.getNombreOriginal(),
                    "tipoMime", archivo.getTipoMime(),
                    "tamanioBytes", archivo.getTamanioBytes(),
                    "fechaSubida", archivo.getFechaSubida().toString(),
                    "idTareaOficina", archivo.getTareaOficina().getIdTareaOficina()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{idArchivo}")
    public ResponseEntity<?> deleteArchivo(@PathVariable Long idArchivo) {
        try {
            archivoAdjuntoService.deleteArchivo(idArchivo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo eliminar el archivo");
        }
    }
}
