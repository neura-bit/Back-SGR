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
}
