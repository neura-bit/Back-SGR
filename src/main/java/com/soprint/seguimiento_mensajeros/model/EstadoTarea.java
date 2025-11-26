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
}
