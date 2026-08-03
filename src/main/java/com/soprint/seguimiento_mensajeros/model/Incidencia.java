package com.soprint.seguimiento_mensajeros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Incidencia reportada por un mensajero al finalizar una tarea.
 *
 * Es puramente informativa: no altera el estado de la tarea ni interrumpe el
 * flujo operativo. Su finalidad es el análisis periódico de la calidad de los
 * datos cargados por quienes crean o mantienen tareas y clientes.
 */
@Entity
@Table(name = "incidencia")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIncidencia;

    // EAGER porque en el perfil de producción open-in-view está en false y el
    // mapeo a DTO ocurre fuera de la transacción.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tarea", nullable = false)
    @JsonIgnore
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente")
    @JsonIgnore
    private Cliente cliente;

    /** Mensajero que reporta. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_mensajero")
    @JsonIgnore
    private Usuario mensajero;

    /**
     * Usuario al que se atribuye la incidencia, congelado en el momento del
     * reporte. Se guarda como copia y no se recalcula: si después alguien
     * corrige los datos del cliente, el informe histórico debe seguir señalando
     * a quien los cargó mal, no a quien los arregló.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_responsable")
    @JsonIgnore
    private Usuario responsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 50)
    private MotivoIncidencia motivo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    public Long getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Long idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getMensajero() {
        return mensajero;
    }

    public void setMensajero(Usuario mensajero) {
        this.mensajero = mensajero;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public MotivoIncidencia getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoIncidencia motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }
}
