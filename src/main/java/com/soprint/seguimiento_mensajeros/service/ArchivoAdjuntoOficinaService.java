package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjuntoOficina;
import com.soprint.seguimiento_mensajeros.model.TareaOficina;
import com.soprint.seguimiento_mensajeros.DTO.UploadResultDTO;
import com.soprint.seguimiento_mensajeros.repository.ArchivoAdjuntoOficinaRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaOficinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArchivoAdjuntoOficinaService implements IArchivoAdjuntoOficinaService {

    @Autowired
    private ArchivoAdjuntoOficinaRepository archivoAdjuntoRepository;

    @Autowired
    private TareaOficinaRepository tareaOficinaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ArchivoAdjuntoOficina uploadFile(MultipartFile file, Long idTareaOficina) {
        TareaOficina tarea = tareaOficinaRepository.findById(idTareaOficina)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        // Almacenar el archivo físicamente, usamos el prefijo 'oficina_' para separar
        // carpetas
        String nombreAlmacenado = fileStorageService.guardarArchivo(file, idTareaOficina + 1000000L); // Truco para
                                                                                                      // evitar colisión
                                                                                                      // de carpetas

        // Guardar metadata en base de datos
        ArchivoAdjuntoOficina archivoAdjunto = new ArchivoAdjuntoOficina();
        archivoAdjunto.setTareaOficina(tarea);
        archivoAdjunto.setNombreOriginal(file.getOriginalFilename());
        archivoAdjunto.setNombreAlmacenado(nombreAlmacenado);
        archivoAdjunto.setTipoMime(file.getContentType());
        archivoAdjunto.setTamanioBytes(file.getSize());
        archivoAdjunto.setFechaSubida(LocalDateTime.now());

        return archivoAdjuntoRepository.save(archivoAdjunto);
    }

    @Override
    public UploadResultDTO uploadFiles(List<MultipartFile> files, Long idTareaOficina) {
        UploadResultDTO resultado = new UploadResultDTO();
        resultado.setTotalRecibidos(files.size());

        for (MultipartFile file : files) {
            try {
                ArchivoAdjuntoOficina archivosGuardado = uploadFile(file, idTareaOficina);
                resultado.getArchivosSubidos().add(archivosGuardado);
            } catch (Exception e) {
                String nombreArchivo = file.getOriginalFilename() != null ? file.getOriginalFilename() : "desconocido";
                resultado.getArchivosFallidos().add(
                        new UploadResultDTO.ArchivoErrorDTO(nombreArchivo, e.getMessage()));
            }
        }

        resultado.setTotalExitosos(resultado.getArchivosSubidos().size());
        resultado.setTotalFallidos(resultado.getArchivosFallidos().size());

        return resultado;
    }

    @Override
    public ArchivoAdjuntoOficina obtenerArchivo(Long idArchivo) {
        return archivoAdjuntoRepository.findById(idArchivo)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado con id: " + idArchivo));
    }

    @Override
    public Resource loadFileAsResource(Long idArchivo) {
        ArchivoAdjuntoOficina archivo = obtenerArchivo(idArchivo);
        return fileStorageService.cargarArchivo(
                archivo.getTareaOficina().getIdTareaOficina() + 1000000L,
                archivo.getNombreAlmacenado());
    }

    @Override
    public List<ArchivoAdjuntoOficina> findArchivosByTarea(Long idTareaOficina) {
        return archivoAdjuntoRepository.findByTareaOficinaIdTareaOficina(idTareaOficina);
    }

    @Override
    public void deleteArchivo(Long idArchivo) {
        ArchivoAdjuntoOficina archivo = archivoAdjuntoRepository.findById(idArchivo)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        // Eliminar archivo físico
        fileStorageService.eliminarArchivo(archivo.getTareaOficina().getIdTareaOficina() + 1000000L,
                archivo.getNombreAlmacenado());

        // Eliminar metadata de base de datos
        archivoAdjuntoRepository.delete(archivo);
    }
}
