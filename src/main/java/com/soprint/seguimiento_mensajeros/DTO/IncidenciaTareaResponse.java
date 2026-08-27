package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.IncidenciaTarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.time.LocalDateTime;

/**
 * Detalle de una incidencia de tarea. Como en el resto de DTOs, de los usuarios
 * solo se publican id y nombre para no exponer la entidad completa.
 *
 * Lleva el nombre y el código de la tarea porque es lo que el asesor necesita
 * para identificarla de un vistazo en su panel de novedades.
 */
public class IncidenciaTareaResponse {

    private Long idIncidenciaTarea;

    private Long idTarea;
    private String tareaCodigo;
    private String tareaNombre;
    private String tareaEstado;
    private String tipoOperacion;

    private Long idCliente;
    private String clienteNombre;

    private Long idSupervisor;
    private String supervisor;

    private Long idAsesorResponsable;
    private String asesorResponsable;

    private String motivo;
    private String motivoEtiqueta;
    private String descripcion;

    private String estado;
    private String estadoEtiqueta;

    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
    private String resueltaPor;

    public static IncidenciaTareaResponse fromEntity(IncidenciaTarea incidencia) {
        IncidenciaTareaResponse dto = new IncidenciaTareaResponse();

        dto.idIncidenciaTarea = incidencia.getIdIncidenciaTarea();

        if (incidencia.getTarea() != null) {
            dto.idTarea = incidencia.getTarea().getIdTarea();
            dto.tareaCodigo = incidencia.getTarea().getCodigo();
            dto.tareaNombre = incidencia.getTarea().getNombre();

            if (incidencia.getTarea().getEstadoTarea() != null) {
                dto.tareaEstado = incidencia.getTarea().getEstadoTarea().getNombre();
            }
            if (incidencia.getTarea().getTipoOperacion() != null) {
                dto.tipoOperacion = incidencia.getTarea().getTipoOperacion().getNombre();
            }
            if (incidencia.getTarea().getCliente() != null) {
                dto.idCliente = incidencia.getTarea().getCliente().getIdCliente();
                dto.clienteNombre = incidencia.getTarea().getCliente().getNombre();
            }
        }

        Usuario supervisor = incidencia.getSupervisor();
        if (supervisor != null) {
            dto.idSupervisor = supervisor.getIdUsuario();
            dto.supervisor = nombreCompleto(supervisor);
        }

        Usuario asesor = incidencia.getAsesorResponsable();
        if (asesor != null) {
            dto.idAsesorResponsable = asesor.getIdUsuario();
            dto.asesorResponsable = nombreCompleto(asesor);
        }

        if (incidencia.getMotivo() != null) {
            dto.motivo = incidencia.getMotivo().name();
            dto.motivoEtiqueta = incidencia.getMotivo().getEtiqueta();
        }
        dto.descripcion = incidencia.getDescripcion();

        if (incidencia.getEstado() != null) {
            dto.estado = incidencia.getEstado().name();
            dto.estadoEtiqueta = incidencia.getEstado().getEtiqueta();
        }

        dto.fechaReporte = incidencia.getFechaReporte();
        dto.fechaResolucion = incidencia.getFechaResolucion();
        if (incidencia.getResueltaPor() != null) {
            dto.resueltaPor = nombreCompleto(incidencia.getResueltaPor());
        }

        return dto;
    }

    private static String nombreCompleto(Usuario usuario) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "";
        String apellido = usuario.getApellido() != null ? usuario.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }

    public Long getIdIncidenciaTarea() {
        return idIncidenciaTarea;
    }

    public Long getIdTarea() {
        return idTarea;
    }

    public String getTareaCodigo() {
        return tareaCodigo;
    }

    public String getTareaNombre() {
        return tareaNombre;
    }

    public String getTareaEstado() {
        return tareaEstado;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public Long getIdSupervisor() {
        return idSupervisor;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public Long getIdAsesorResponsable() {
        return idAsesorResponsable;
    }

    public String getAsesorResponsable() {
        return asesorResponsable;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getMotivoEtiqueta() {
        return motivoEtiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public String getEstadoEtiqueta() {
        return estadoEtiqueta;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public String getResueltaPor() {
        return resueltaPor;
    }
}
