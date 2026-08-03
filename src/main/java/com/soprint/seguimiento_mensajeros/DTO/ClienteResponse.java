package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Cliente.
 *
 * Existe para poder exponer los datos de auditoría sin serializar la entidad
 * Usuario completa, que incluye la contraseña y el token FCM. Los nombres de
 * los campos propios del cliente se mantienen idénticos a los de la entidad
 * para no romper a los consumidores existentes.
 */
public class ClienteResponse {

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

    // Auditoría
    private Long creadoPorId;
    private String creadoPor;
    private LocalDateTime fechaCreacion;

    private Long modificadoPorId;
    private String modificadoPor;
    private LocalDateTime fechaModificacion;

    /** Respuesta sin datos de auditoría (para roles distintos de ADMIN). */
    public static ClienteResponse fromEntity(Cliente cliente) {
        return fromEntity(cliente, false);
    }

    /**
     * @param incluirAuditoria solo debe ser true para administradores; de lo
     *                         contrario los campos de auditoría se omiten por
     *                         completo de la respuesta.
     */
    public static ClienteResponse fromEntity(Cliente cliente, boolean incluirAuditoria) {
        ClienteResponse dto = new ClienteResponse();

        dto.idCliente = cliente.getIdCliente();
        dto.nombre = cliente.getNombre();
        dto.telefono = cliente.getTelefono();
        dto.rucCi = cliente.getRucCi();
        dto.direccion = cliente.getDireccion();
        dto.ciudad = cliente.getCiudad();
        dto.latitud = cliente.getLatitud();
        dto.longitud = cliente.getLongitud();
        dto.detalle = cliente.getDetalle();
        dto.correo = cliente.getCorreo();

        if (!incluirAuditoria) {
            return dto;
        }

        Usuario creador = cliente.getCreadoPor();
        if (creador != null) {
            dto.creadoPorId = creador.getIdUsuario();
            dto.creadoPor = nombreCompleto(creador);
        }
        dto.fechaCreacion = cliente.getFechaCreacion();

        Usuario modificador = cliente.getModificadoPor();
        if (modificador != null) {
            dto.modificadoPorId = modificador.getIdUsuario();
            dto.modificadoPor = nombreCompleto(modificador);
        }
        dto.fechaModificacion = cliente.getFechaModificacion();

        return dto;
    }

    private static String nombreCompleto(Usuario usuario) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "";
        String apellido = usuario.getApellido() != null ? usuario.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }

    // Getters
    public Long getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRucCi() {
        return rucCi;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getCorreo() {
        return correo;
    }

    public Long getCreadoPorId() {
        return creadoPorId;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Long getModificadoPorId() {
        return modificadoPorId;
    }

    public String getModificadoPor() {
        return modificadoPor;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
}
