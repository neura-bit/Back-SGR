package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjuntoOficina;
import java.util.ArrayList;
import java.util.List;

public class UploadResultDTO {

    private List<ArchivoAdjuntoOficina> archivosSubidos = new ArrayList<>();
    private List<ArchivoErrorDTO> archivosFallidos = new ArrayList<>();
    private int totalRecibidos;
    private int totalExitosos;
    private int totalFallidos;

    public static class ArchivoErrorDTO {
        private String nombreArchivo;
        private String error;

        public ArchivoErrorDTO(String nombreArchivo, String error) {
            this.nombreArchivo = nombreArchivo;
            this.error = error;
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public void setNombreArchivo(String nombreArchivo) {
            this.nombreArchivo = nombreArchivo;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    // Getters y Setters

    public List<ArchivoAdjuntoOficina> getArchivosSubidos() {
        return archivosSubidos;
    }

    public void setArchivosSubidos(List<ArchivoAdjuntoOficina> archivosSubidos) {
        this.archivosSubidos = archivosSubidos;
    }

    public List<ArchivoErrorDTO> getArchivosFallidos() {
        return archivosFallidos;
    }

    public void setArchivosFallidos(List<ArchivoErrorDTO> archivosFallidos) {
        this.archivosFallidos = archivosFallidos;
    }

    public int getTotalRecibidos() {
        return totalRecibidos;
    }

    public void setTotalRecibidos(int totalRecibidos) {
        this.totalRecibidos = totalRecibidos;
    }

    public int getTotalExitosos() {
        return totalExitosos;
    }

    public void setTotalExitosos(int totalExitosos) {
        this.totalExitosos = totalExitosos;
    }

    public int getTotalFallidos() {
        return totalFallidos;
    }

    public void setTotalFallidos(int totalFallidos) {
        this.totalFallidos = totalFallidos;
    }
}
