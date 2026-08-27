package com.soprint.seguimiento_mensajeros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Incidencia que un supervisor reporta sobre una tarea mientras ordena las
 * rutas, antes de que el mensajero salga. El caso que la originó: la factura
 * física no está lista y debe viajar junto con el producto.
 *
 * No confundir con {@link Incidencia}, que es del mensajero AL CERRAR la tarea
 * y sirve para analizar la calidad de los datos hacia atrás. Esta es del
 * supervisor AL PREPARAR, avisa al asesor que creó la tarea y tiene estado.
 *
 * No bloquea nada: la tarea se asigna y se despacha igual. Es un aviso y,
 * sobre todo, una constancia de que la tarea salió incompleta.
 */
@Entity
@Table(name = "incidencia_tarea")
public class IncidenciaTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIncidenciaTarea;

    // EAGER por el mismo motivo que en Incidencia: en producción open-in-view
    // está en false y el mapeo a DTO ocurre fuera de la transacción.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tarea", nullable = false)
    @JsonIgnore
    private Tarea tarea;

    /** Supervisor que detecta el problema al armar la ruta. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_supervisor")
    @JsonIgnore
    private Usuario supervisor;

    /**
     * Asesor al que se le avisa, copiado de la tarea en el momento del reporte
     * y no recalculado después.
     *
     * Es el mismo criterio que usa IncidenciaService.resolverResponsable: el
     * informe histórico debe seguir señalando a quien creó la tarea incompleta,
     * aunque más adelante alguien la edite o la reasigne.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asesor_responsable")
    @JsonIgnore
    private Usuario asesorResponsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 50)
    private MotivoIncidenciaTarea motivo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoIncidenciaTarea estado = EstadoIncidenciaTarea.ABIERTA;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_resuelta_por")
    @JsonIgnore
    private Usuario resueltaPor;

    public Long getIdIncidenciaTarea() {
        return idIncidenciaTarea;
    }

    public void setIdIncidenciaTarea(Long idIncidenciaTarea) {
        this.idIncidenciaTarea = idIncidenciaTarea;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public Usuario getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Usuario supervisor) {
        this.supervisor = supervisor;
    }

    public Usuario getAsesorResponsable() {
        return asesorResponsable;
    }

    public void setAsesorResponsable(Usuario asesorResponsable) {
        this.asesorResponsable = asesorResponsable;
    }

    public MotivoIncidenciaTarea getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoIncidenciaTarea motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoIncidenciaTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidenciaTarea estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public Usuario getResueltaPor() {
        return resueltaPor;
    }

    public void setResueltaPor(Usuario resueltaPor) {
        this.resueltaPor = resueltaPor;
    }
}
