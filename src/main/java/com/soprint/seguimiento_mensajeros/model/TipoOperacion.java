package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

@Entity
public class TipoOperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_operacion")
    private Long idTipoOperacion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    public Long getIdTipoOperacion() {
        return idTipoOperacion;
    }

    public void setIdTipoOperacion(Long idTipoOperacion) {
        this.idTipoOperacion = idTipoOperacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoOperacion(Long idTipoOperacion, String nombre) {
        this.idTipoOperacion = idTipoOperacion;
        this.nombre = nombre;
    }

    public TipoOperacion() {
    }
}
