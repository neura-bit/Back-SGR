package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.Incidencia;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.time.LocalDateTime;

/**
 * Detalle de una incidencia. Como en el resto de DTOs, de los usuarios solo se
 * publican id y nombre para no exponer la entidad completa.
 */
public class IncidenciaResponse {

    private Long idIncidencia;

    private Long idTarea;
    private String tareaCodigo;
    private String tareaNombre;

    private Long idCliente;
    private String clienteNombre;
    private String clienteDireccion;

    private Long idMensajero;
    private String mensajero;

    private Long idResponsable;
    private String responsable;

    private String motivo;
    private String motivoEtiqueta;
    private String descripcion;
    private LocalDateTime fechaReporte;

    public static IncidenciaResponse fromEntity(Incidencia incidencia) {
        IncidenciaResponse dto = new IncidenciaResponse();

        dto.idIncidencia = incidencia.getIdIncidencia();

        if (incidencia.getTarea() != null) {
            dto.idTarea = incidencia.getTarea().getIdTarea();
            dto.tareaCodigo = incidencia.getTarea().getCodigo();
            dto.tareaNombre = incidencia.getTarea().getNombre();
        }

        if (incidencia.getCliente() != null) {
            dto.idCliente = incidencia.getCliente().getIdCliente();
            dto.clienteNombre = incidencia.getCliente().getNombre();
            dto.clienteDireccion = incidencia.getCliente().getDireccion();
        }

        Usuario mensajero = incidencia.getMensajero();
        if (mensajero != null) {
            dto.idMensajero = mensajero.getIdUsuario();
            dto.mensajero = nombreCompleto(mensajero);
        }

        Usuario responsable = incidencia.getResponsable();
        if (responsable != null) {
            dto.idResponsable = responsable.getIdUsuario();
            dto.responsable = nombreCompleto(responsable);
        }

        if (incidencia.getMotivo() != null) {
            dto.motivo = incidencia.getMotivo().name();
            dto.motivoEtiqueta = incidencia.getMotivo().getEtiqueta();
        }
        dto.descripcion = incidencia.getDescripcion();
        dto.fechaReporte = incidencia.getFechaReporte();

        return dto;
    }

    private static String nombreCompleto(Usuario usuario) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "";
        String apellido = usuario.getApellido() != null ? usuario.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }

    public Long getIdIncidencia() {
        return idIncidencia;
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

    public Long getIdCliente() {
        return idCliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public Long getIdMensajero() {
        return idMensajero;
    }

    public String getMensajero() {
        return mensajero;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public String getResponsable() {
        return responsable;
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

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }
}
