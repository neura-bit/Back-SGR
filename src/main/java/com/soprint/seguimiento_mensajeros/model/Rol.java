package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id_rol;
    public String nombre;
    @OneToMany(mappedBy = "rol")
    public List<Usuario> listUsuarios;
}
