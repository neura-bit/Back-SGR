package com.soprint.seguimiento_mensajeros.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tarea_oficina")
public class TareaOficina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTareaOficina;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_creador", nullable = false)
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoTareaOficina estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoTareaOficina tipo;

    @OneToMany(mappedBy = "tareaOficina", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ArchivoAdjuntoOficina> archivosAdjuntos = new ArrayList<>();

    public TareaOficina() {
    }

    public TareaOficina(String nombre, String descripcion, Usuario creador, Usuario responsable,
            LocalDateTime fechaLimite, LocalDateTime fechaCreacion, EstadoTareaOficina estado,
            TipoTareaOficina tipo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creador = creador;
        this.responsable = responsable;
        this.fechaLimite = fechaLimite;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.tipo = tipo;
    }

    public Long getIdTareaOficina() {
        return idTareaOficina;
    }

    public void setIdTareaOficina(Long idTareaOficina) {
        this.idTareaOficina = idTareaOficina;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoTareaOficina getEstado() {
        return estado;
    }

    public void setEstado(EstadoTareaOficina estado) {
        this.estado = estado;
    }

    public TipoTareaOficina getTipo() {
        return tipo;
    }

    public void setTipo(TipoTareaOficina tipo) {
        this.tipo = tipo;
    }

    public List<ArchivoAdjuntoOficina> getArchivosAdjuntos() {
        return archivosAdjuntos;
    }

    public void setArchivosAdjuntos(List<ArchivoAdjuntoOficina> archivosAdjuntos) {
        this.archivosAdjuntos = archivosAdjuntos;
    }
}
