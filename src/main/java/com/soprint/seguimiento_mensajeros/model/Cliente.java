package com.soprint.seguimiento_mensajeros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private String detalle;
    private String correo;

    // Auditoría. Se marcan con @JsonIgnore por dos motivos: evitar que el
    // usuario completo (incluida su contraseña) viaje en las respuestas, y
    // evitar que estos campos puedan falsearse desde el cuerpo de la petición.
    // Para exponerlos se usa ClienteResponse.
    // EAGER a propósito: en el perfil de producción open-in-view está en false,
    // por lo que una relación LAZY fallaría al mapearse fuera de la transacción.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_creado_por")
    @JsonIgnore
    private Usuario creadoPor;

    @Column(name = "fecha_creacion")
    @JsonIgnore
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_modificado_por")
    @JsonIgnore
    private Usuario modificadoPor;

    @Column(name = "fecha_modificacion")
    @JsonIgnore
    private LocalDateTime fechaModificacion;

    public Usuario getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getModificadoPor() {
        return modificadoPor;
    }

    public void setModificadoPor(Usuario modificadoPor) {
        this.modificadoPor = modificadoPor;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRucCi() {
        return rucCi;
    }

    public void setRucCi(String rucCi) {
        this.rucCi = rucCi;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
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

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Cliente(Long idCliente, String nombre, String telefono, String rucCi, String direccion, String ciudad,
            Double latitud, Double longitud, String detalle, String correo) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rucCi = rucCi;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.detalle = detalle;
        this.correo = correo;
    }

    public Cliente() {
    }
}
