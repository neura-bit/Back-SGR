package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idUsuario;
    public String nombre;
    public String apellido;
    public String telefono;
    public String username;
    public String password;
    public Boolean estado;
    public LocalDateTime fechaCreacion;
    @ManyToOne
    @JoinColumn(name="idSucursal")
    public Sucursal sucursal;
    @ManyToOne
    @JoinColumn(name="idRol")
    public Rol rol;
}
