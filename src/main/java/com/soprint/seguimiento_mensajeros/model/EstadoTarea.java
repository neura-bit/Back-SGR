package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class EstadoTarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoTarea;
    private String nombre; // Ej: "CREADA", "EN_PROCESO", "FINALIZADA"

    public Long getIdEstadoTarea() {
        return idEstadoTarea;
    }

    public void setIdEstadoTarea(Long idEstadoTarea) {
        this.idEstadoTarea = idEstadoTarea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoTarea(Long idEstadoTarea, String nombre) {
        this.idEstadoTarea = idEstadoTarea;
        this.nombre = nombre;
    }

    public EstadoTarea() {
    }
}
