package com.soprint.seguimiento_mensajeros.DTO;

import lombok.Builder;

@Builder
public class PosicionRequest {

    private Long idMensajero;
    private Long idTarea;   // mientras esté en ruta
    private Double latitud;
    private Double longitud;

    public Long getIdMensajero() {
        return idMensajero;
    }

    public void setIdMensajero(Long idMensajero) {
        this.idMensajero = idMensajero;
    }

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public PosicionRequest(Long idMensajero, Long idTarea, Double latitud, Double longitud) {
        this.idMensajero = idMensajero;
        this.idTarea = idTarea;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public PosicionRequest() {
    }
}
