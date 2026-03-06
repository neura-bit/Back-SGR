package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjuntoOficina;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.util.List;

public interface IArchivoAdjuntoOficinaService {
    ArchivoAdjuntoOficina uploadFile(MultipartFile file, Long idTareaOficina);

    ArchivoAdjuntoOficina obtenerArchivo(Long idArchivo);

    Resource loadFileAsResource(Long idArchivo);

    List<ArchivoAdjuntoOficina> findArchivosByTarea(Long idTareaOficina);

    void deleteArchivo(Long idArchivo);
}
