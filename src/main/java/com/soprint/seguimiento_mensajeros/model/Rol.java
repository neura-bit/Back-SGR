package com.soprint.seguimiento_mensajeros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id_rol;
    public String nombre;
    @OneToMany(mappedBy = "rol")
    @JsonIgnore
    public List<Usuario> listUsuarios;

    public Long getId_rol() {
        return id_rol;
    }

    public void setId_rol(Long id_rol) {
        this.id_rol = id_rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Usuario> getListUsuarios() {
        return listUsuarios;
    }

    public void setListUsuarios(List<Usuario> listUsuarios) {
        this.listUsuarios = listUsuarios;
    }

    public Rol(Long id_rol, String nombre, List<Usuario> listUsuarios) {
        this.id_rol = id_rol;
        this.nombre = nombre;
        this.listUsuarios = listUsuarios;
    }

    public Rol() {
    }
}
