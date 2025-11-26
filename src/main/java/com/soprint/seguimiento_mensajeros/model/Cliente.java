package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;
    private String nombre;
    private String telefono;
    private String rucCi;
    private String direccion;
    private String ciudad;
    private Double latitud;
    private Double longitud;

}
