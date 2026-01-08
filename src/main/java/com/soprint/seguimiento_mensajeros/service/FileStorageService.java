package com.soprint.seguimiento_mensajeros.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    // Tipos MIME permitidos
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif");

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento", e);
        }
    }

    /**
     * Guarda un archivo en el sistema de archivos.
     * 
     * @param file    El archivo a guardar
     * @param idTarea ID de la tarea para organizar en subcarpetas
     * @return Nombre único del archivo almacenado
     */
    public String guardarArchivo(MultipartFile file, Long idTarea) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Tipos aceptados: PDF, JPEG, PNG, GIF");
        }

        try {
            // Crear subcarpeta para la tarea
            Path tareaDir = rootLocation.resolve(String.valueOf(idTarea));
            Files.createDirectories(tareaDir);

            // Generar nombre único
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String nombreAlmacenado = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path destinationFile = tareaDir.resolve(nombreAlmacenado).normalize().toAbsolutePath();

            // Verificar que no se escape del directorio raíz (ambas rutas absolutas)
            Path rootAbsolute = rootLocation.toAbsolutePath().normalize();
            if (!destinationFile.startsWith(rootAbsolute)) {
                throw new SecurityException("Intento de guardar archivo fuera del directorio permitido");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return nombreAlmacenado;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Carga un archivo como Resource para descarga.
     * 
     * @param idTarea          ID de la tarea
     * @param nombreAlmacenado Nombre del archivo en disco
     * @return Resource del archivo
     */
    public Resource cargarArchivo(Long idTarea, String nombreAlmacenado) {
        try {
            Path file = rootLocation.resolve(String.valueOf(idTarea)).resolve(nombreAlmacenado);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se puede leer el archivo: " + nombreAlmacenado);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar el archivo: " + nombreAlmacenado, e);
        }
    }

    /**
     * Elimina un archivo del sistema de archivos.
     * 
     * @param idTarea          ID de la tarea
     * @param nombreAlmacenado Nombre del archivo en disco
     * @return true si se eliminó correctamente
     */
    public boolean eliminarArchivo(Long idTarea, String nombreAlmacenado) {
        try {
            Path file = rootLocation.resolve(String.valueOf(idTarea)).resolve(nombreAlmacenado);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + nombreAlmacenado, e);
        }
    }

    /**
     * Obtiene la ruta completa de un archivo.
     */
    public Path getRutaArchivo(Long idTarea, String nombreAlmacenado) {
        return rootLocation.resolve(String.valueOf(idTarea)).resolve(nombreAlmacenado);
    }
}
