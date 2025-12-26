package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarea;

    // ===== RELACIONES PRINCIPALES =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_tarea", nullable = false)
    private EstadoTarea estadoTarea;

    // ===== USUARIOS RELACIONADOS =====
    // Asesor que crea la tarea
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asesor_crea", nullable = false)
    private Usuario asesorCrea;

    // Mensajero asignado (puede iniciar en null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensajero_asignado")
    private Usuario mensajeroAsignado;

    // Supervisor que asigna (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supervisor_asigna")
    private Usuario supervisorAsigna;

    // ===== CAMPOS DE LA TAREA =====

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // Tiempo total (por ejemplo en minutos) desde fechaCreacion hasta fechaFin
    @Column(name = "tiempo_total")
    private Long tiempoTotal;

    // Tiempo de ejecución (en minutos) desde fechaInicio hasta fechaFin
    @Column(name = "tiempo_ejecucion")
    private Long tiempoEjecucion;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "observacion", length = 2000)
    private String observacion;

    @Column(name = "proceso", length = 255)
    private String proceso;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    // SOLO NÚMEROS DE 4 DÍGITOS: "0001" ..."9999"
    @Pattern(regexp = "\\d{4}", message = "El código debe tener exactamente 4 dígitos numéricos")
    @Column(name = "codigo", length = 4, nullable = false)
    private String codigo;

    // Indicador si la entrega fue a tiempo (fechaFin <= fechaLimite)
    @Column(name = "entrega_a_tiempo")
    private Boolean entregaATiempo;

    // Tiempo de respuesta en minutos (desde fechaCreacion/asignación hasta
    // fechaInicio)
    @Column(name = "tiempo_respuesta")
    private Long tiempoRespuesta;

    public Long getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Long idTarea) {
        this.idTarea = idTarea;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public EstadoTarea getEstadoTarea() {
        return estadoTarea;
    }

    public void setEstadoTarea(EstadoTarea estadoTarea) {
        this.estadoTarea = estadoTarea;
    }

    public Usuario getAsesorCrea() {
        return asesorCrea;
    }

    public void setAsesorCrea(Usuario asesorCrea) {
        this.asesorCrea = asesorCrea;
    }

    public Usuario getMensajeroAsignado() {
        return mensajeroAsignado;
    }

    public void setMensajeroAsignado(Usuario mensajeroAsignado) {
        this.mensajeroAsignado = mensajeroAsignado;
    }

    public Usuario getSupervisorAsigna() {
        return supervisorAsigna;
    }

    public void setSupervisorAsigna(Usuario supervisorAsigna) {
        this.supervisorAsigna = supervisorAsigna;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Long getTiempoTotal() {
        return tiempoTotal;
    }

    public void setTiempoTotal(Long tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }

    public Long getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public void setTiempoEjecucion(Long tiempoEjecucion) {
        this.tiempoEjecucion = tiempoEjecucion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getProceso() {
        return proceso;
    }

    public void setProceso(String proceso) {
        this.proceso = proceso;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public @Pattern(regexp = "\\d{4}", message = "El código debe tener exactamente 4 dígitos numéricos") String getCodigo() {
        return codigo;
    }

    public void setCodigo(
            @Pattern(regexp = "\\d{4}", message = "El código debe tener exactamente 4 dígitos numéricos") String codigo) {
        this.codigo = codigo;
    }

    public Boolean getEntregaATiempo() {
        return entregaATiempo;
    }

    public void setEntregaATiempo(Boolean entregaATiempo) {
        this.entregaATiempo = entregaATiempo;
    }

    public Long getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    public void setTiempoRespuesta(Long tiempoRespuesta) {
        this.tiempoRespuesta = tiempoRespuesta;
    }

    public Tarea(Long idTarea, TipoOperacion tipoOperacion, Categoria categoria, Cliente cliente,
            EstadoTarea estadoTarea, Usuario asesorCrea, Usuario mensajeroAsignado, Usuario supervisorAsigna,
            String nombre, LocalDateTime fechaCreacion, LocalDateTime fechaLimite, LocalDateTime fechaFin,
            Long tiempoTotal, Long tiempoEjecucion, String comentario, String observacion, String proceso,
            LocalDateTime fechaInicio, String codigo, Boolean entregaATiempo, Long tiempoRespuesta) {
        this.idTarea = idTarea;
        this.tipoOperacion = tipoOperacion;
        this.categoria = categoria;
        this.cliente = cliente;
        this.estadoTarea = estadoTarea;
        this.asesorCrea = asesorCrea;
        this.mensajeroAsignado = mensajeroAsignado;
        this.supervisorAsigna = supervisorAsigna;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.fechaLimite = fechaLimite;
        this.fechaFin = fechaFin;
        this.tiempoTotal = tiempoTotal;
        this.tiempoEjecucion = tiempoEjecucion;
        this.comentario = comentario;
        this.observacion = observacion;
        this.proceso = proceso;
        this.fechaInicio = fechaInicio;
        this.codigo = codigo;
        this.entregaATiempo = entregaATiempo;
        this.tiempoRespuesta = tiempoRespuesta;
    }

    public Tarea() {
    }
}
