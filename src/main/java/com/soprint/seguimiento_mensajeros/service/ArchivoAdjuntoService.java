package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjunto;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.repository.ArchivoAdjuntoRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class ArchivoAdjuntoService {

    private final ArchivoAdjuntoRepository archivoAdjuntoRepository;
    private final TareaRepository tareaRepository;
    private final FileStorageService fileStorageService;

    public ArchivoAdjuntoService(ArchivoAdjuntoRepository archivoAdjuntoRepository,
            TareaRepository tareaRepository,
            FileStorageService fileStorageService) {
        this.archivoAdjuntoRepository = archivoAdjuntoRepository;
        this.tareaRepository = tareaRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Sube un archivo y lo asocia a una tarea.
     */
    public ArchivoAdjunto subirArchivo(MultipartFile file, Long idTarea) {
        // Verificar que la tarea existe
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // Guardar archivo físico
        String nombreAlmacenado = fileStorageService.guardarArchivo(file, idTarea);

        // Crear registro en BD
        ArchivoAdjunto archivo = new ArchivoAdjunto(
                tarea,
                file.getOriginalFilename(),
                nombreAlmacenado,
                file.getContentType(),
                file.getSize());

        return archivoAdjuntoRepository.save(archivo);
    }

    /**
     * Lista todos los archivos de una tarea.
     */
    @Transactional(readOnly = true)
    public List<ArchivoAdjunto> listarArchivos(Long idTarea) {
        return archivoAdjuntoRepository.findByTareaIdTarea(idTarea);
    }

    /**
     * Obtiene un archivo por su ID.
     */
    @Transactional(readOnly = true)
    public ArchivoAdjunto obtenerArchivo(Long idArchivo) {
        return archivoAdjuntoRepository.findById(idArchivo)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado con id: " + idArchivo));
    }

    /**
     * Descarga un archivo como Resource.
     */
    @Transactional(readOnly = true)
    public Resource descargarArchivo(Long idArchivo) {
        ArchivoAdjunto archivo = obtenerArchivo(idArchivo);
        return fileStorageService.cargarArchivo(
                archivo.getTarea().getIdTarea(),
                archivo.getNombreAlmacenado());
    }

    /**
     * Elimina un archivo (físico y metadatos).
     */
    public void eliminarArchivo(Long idArchivo) {
        ArchivoAdjunto archivo = obtenerArchivo(idArchivo);

        // Eliminar archivo físico
        fileStorageService.eliminarArchivo(
                archivo.getTarea().getIdTarea(),
                archivo.getNombreAlmacenado());

        // Eliminar registro de BD
        archivoAdjuntoRepository.delete(archivo);
    }

    /**
     * Elimina todos los archivos de una tarea.
     */
    public void eliminarArchivosDeTarea(Long idTarea) {
        List<ArchivoAdjunto> archivos = archivoAdjuntoRepository.findByTareaIdTarea(idTarea);

        for (ArchivoAdjunto archivo : archivos) {
            fileStorageService.eliminarArchivo(idTarea, archivo.getNombreAlmacenado());
        }

        archivoAdjuntoRepository.deleteByTareaIdTarea(idTarea);
    }
}
