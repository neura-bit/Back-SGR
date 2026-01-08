package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjunto;
import com.soprint.seguimiento_mensajeros.model.Tarea;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO optimizado para respuestas de Tarea.
 * Reduce el tamaño de la respuesta y evita exponer datos sensibles.
 */
public class TareaResponse {

    private Long idTarea;
    private String nombre;
    private String codigo;

    // Tipo Operación (solo nombre)
    private Long idTipoOperacion;
    private String tipoOperacion;

    // Categoría (solo nombre)
    private Long idCategoria;
    private String categoria;

    // Cliente (datos básicos)
    private Long idCliente;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteDireccion;
    private String clienteCiudad;
    private Double clienteLatitud;
    private Double clienteLongitud;

    // Estado
    private Long idEstadoTarea;
    private String estadoTarea;

    // Usuarios (solo id y nombre completo)
    private Long idAsesorCrea;
    private String asesorCrea;

    private Long idMensajeroAsignado;
    private String mensajeroAsignado;

    private Long idSupervisorAsigna;
    private String supervisorAsigna;

    // Fechas y tiempos
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long tiempoTotal;
    private Long tiempoEjecucion;
    private Long tiempoRespuesta;
    private Boolean entregaATiempo;

    // Otros campos
    private String comentario;
    private String observacion;
    private String proceso;

    // Archivos adjuntos
    private List<ArchivoAdjuntoResponse> archivosAdjuntos;

    // Constructor vacío
    public TareaResponse() {
    }

    // Constructor desde entidad
    public static TareaResponse fromEntity(Tarea tarea) {
        TareaResponse dto = new TareaResponse();

        dto.idTarea = tarea.getIdTarea();
        dto.nombre = tarea.getNombre();
        dto.codigo = tarea.getCodigo();

        // Tipo Operación
        if (tarea.getTipoOperacion() != null) {
            dto.idTipoOperacion = tarea.getTipoOperacion().getIdTipoOperacion();
            dto.tipoOperacion = tarea.getTipoOperacion().getNombre();
        }

        // Categoría
        if (tarea.getCategoria() != null) {
            dto.idCategoria = tarea.getCategoria().getIdCategoria();
            dto.categoria = tarea.getCategoria().getNombre();
        }

        // Cliente
        if (tarea.getCliente() != null) {
            dto.idCliente = tarea.getCliente().getIdCliente();
            dto.clienteNombre = tarea.getCliente().getNombre();
            dto.clienteTelefono = tarea.getCliente().getTelefono();
            dto.clienteDireccion = tarea.getCliente().getDireccion();
            dto.clienteCiudad = tarea.getCliente().getCiudad();
            dto.clienteLatitud = tarea.getCliente().getLatitud();
            dto.clienteLongitud = tarea.getCliente().getLongitud();
        }

        // Estado
        if (tarea.getEstadoTarea() != null) {
            dto.idEstadoTarea = tarea.getEstadoTarea().getIdEstadoTarea();
            dto.estadoTarea = tarea.getEstadoTarea().getNombre();
        }

        // Asesor
        if (tarea.getAsesorCrea() != null) {
            dto.idAsesorCrea = tarea.getAsesorCrea().getIdUsuario();
            dto.asesorCrea = tarea.getAsesorCrea().getNombre() + " " + tarea.getAsesorCrea().getApellido();
        }

        // Mensajero
        if (tarea.getMensajeroAsignado() != null) {
            dto.idMensajeroAsignado = tarea.getMensajeroAsignado().getIdUsuario();
            dto.mensajeroAsignado = tarea.getMensajeroAsignado().getNombre() + " "
                    + tarea.getMensajeroAsignado().getApellido();
        }

        // Supervisor
        if (tarea.getSupervisorAsigna() != null) {
            dto.idSupervisorAsigna = tarea.getSupervisorAsigna().getIdUsuario();
            dto.supervisorAsigna = tarea.getSupervisorAsigna().getNombre() + " "
                    + tarea.getSupervisorAsigna().getApellido();
        }

        // Fechas y tiempos
        dto.fechaCreacion = tarea.getFechaCreacion();
        dto.fechaLimite = tarea.getFechaLimite();
        dto.fechaInicio = tarea.getFechaInicio();
        dto.fechaFin = tarea.getFechaFin();
        dto.tiempoTotal = tarea.getTiempoTotal();
        dto.tiempoEjecucion = tarea.getTiempoEjecucion();
        dto.tiempoRespuesta = tarea.getTiempoRespuesta();
        dto.entregaATiempo = tarea.getEntregaATiempo();

        // Otros campos
        dto.comentario = tarea.getComentario();
        dto.observacion = tarea.getObservacion();
        dto.proceso = tarea.getProceso();

        // Archivos adjuntos
        if (tarea.getArchivosAdjuntos() != null) {
            dto.archivosAdjuntos = tarea.getArchivosAdjuntos().stream()
                    .map(ArchivoAdjuntoResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return dto;
    }

    // Clase interna para archivos adjuntos
    public static class ArchivoAdjuntoResponse {
        private Long idArchivo;
        private String nombreOriginal;
        private String tipoMime;
        private Long tamanioBytes;
        private LocalDateTime fechaSubida;

        public static ArchivoAdjuntoResponse fromEntity(ArchivoAdjunto archivo) {
            ArchivoAdjuntoResponse dto = new ArchivoAdjuntoResponse();
            dto.idArchivo = archivo.getIdArchivo();
            dto.nombreOriginal = archivo.getNombreOriginal();
            dto.tipoMime = archivo.getTipoMime();
            dto.tamanioBytes = archivo.getTamanioBytes();
            dto.fechaSubida = archivo.getFechaSubida();
            return dto;
        }

        // Getters
        public Long getIdArchivo() {
            return idArchivo;
        }

        public String getNombreOriginal() {
            return nombreOriginal;
        }

        public String getTipoMime() {
            return tipoMime;
        }

        public Long getTamanioBytes() {
            return tamanioBytes;
        }

        public LocalDateTime getFechaSubida() {
            return fechaSubida;
        }
    }

    // Getters
    public Long getIdTarea() {
        return idTarea;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getIdTipoOperacion() {
        return idTipoOperacion;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public String getClienteCiudad() {
        return clienteCiudad;
    }

    public Double getClienteLatitud() {
        return clienteLatitud;
    }

    public Double getClienteLongitud() {
        return clienteLongitud;
    }

    public Long getIdEstadoTarea() {
        return idEstadoTarea;
    }

    public String getEstadoTarea() {
        return estadoTarea;
    }

    public Long getIdAsesorCrea() {
        return idAsesorCrea;
    }

    public String getAsesorCrea() {
        return asesorCrea;
    }

    public Long getIdMensajeroAsignado() {
        return idMensajeroAsignado;
    }

    public String getMensajeroAsignado() {
        return mensajeroAsignado;
    }

    public Long getIdSupervisorAsigna() {
        return idSupervisorAsigna;
    }

    public String getSupervisorAsigna() {
        return supervisorAsigna;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public Long getTiempoTotal() {
        return tiempoTotal;
    }

    public Long getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public Long getTiempoRespuesta() {
        return tiempoRespuesta;
    }

    public Boolean getEntregaATiempo() {
        return entregaATiempo;
    }

    public String getComentario() {
        return comentario;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getProceso() {
        return proceso;
    }

    public List<ArchivoAdjuntoResponse> getArchivosAdjuntos() {
        return archivosAdjuntos;
    }
}
