package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idSucursal;
    public String nombre;
    public String direccion;
    public String ciudad;
    public String telefono;
    @OneToMany(mappedBy = "sucursal")
    public List<Usuario> listUsuarios;
}
