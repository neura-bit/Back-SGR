package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posicion_mensajero_actual")
@Builder
public class PosicionMensajeroActual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_posicion")
    private Long idPosicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mensajero", nullable = false)
    private Usuario mensajero; // Usuario con rol MENSAJERO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarea_actual")
    private Tarea tareaActual; // puede ser null si está sin tarea

    @Column(name = "latitud", nullable = false)
    private Double latitud;

    @Column(name = "longitud", nullable = false)
    private Double longitud;

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    public Long getIdPosicion() {
        return idPosicion;
    }

    public void setIdPosicion(Long idPosicion) {
        this.idPosicion = idPosicion;
    }

    public Usuario getMensajero() {
        return mensajero;
    }

    public void setMensajero(Usuario mensajero) {
        this.mensajero = mensajero;
    }

    public Tarea getTareaActual() {
        return tareaActual;
    }

    public void setTareaActual(Tarea tareaActual) {
        this.tareaActual = tareaActual;
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

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public PosicionMensajeroActual(Long idPosicion, Usuario mensajero, Tarea tareaActual, Double latitud, Double longitud, LocalDateTime fechaUltimaActualizacion) {
        this.idPosicion = idPosicion;
        this.mensajero = mensajero;
        this.tareaActual = tareaActual;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public PosicionMensajeroActual() {
    }
}
