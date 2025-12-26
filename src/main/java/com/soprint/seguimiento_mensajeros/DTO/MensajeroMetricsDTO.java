package com.soprint.seguimiento_mensajeros.DTO;

import java.time.LocalDate;

/**
 * DTO para representar las métricas de rendimiento de un mensajero.
 */
public class MensajeroMetricsDTO {

    private Long idMensajero;
    private String nombreMensajero;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Contadores de tareas
    private Integer totalTareasAsignadas;
    private Integer tareasCompletadas;
    private Integer tareasPendientes;
    private Integer tareasEnProceso;

    // Métricas de cumplimiento
    private Integer entregasATiempo;
    private Integer entregasTardias;
    private Double porcentajeCumplimiento;

    // Tiempos promedio (en minutos)
    private Double tiempoPromedioRespuesta;
    private Double tiempoPromedioEjecucion;
    private Double tiempoPromedioTotal;

    // Porcentaje de tareas completadas
    private Double porcentajeCompletado;

    // Constructor vacío
    public MensajeroMetricsDTO() {
    }

    // Constructor completo
    public MensajeroMetricsDTO(Long idMensajero, String nombreMensajero, LocalDate fechaInicio, LocalDate fechaFin,
            Integer totalTareasAsignadas, Integer tareasCompletadas, Integer tareasPendientes, Integer tareasEnProceso,
            Integer entregasATiempo, Integer entregasTardias, Double porcentajeCumplimiento,
            Double tiempoPromedioRespuesta, Double tiempoPromedioEjecucion, Double tiempoPromedioTotal,
            Double porcentajeCompletado) {
        this.idMensajero = idMensajero;
        this.nombreMensajero = nombreMensajero;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.totalTareasAsignadas = totalTareasAsignadas;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasPendientes = tareasPendientes;
        this.tareasEnProceso = tareasEnProceso;
        this.entregasATiempo = entregasATiempo;
        this.entregasTardias = entregasTardias;
        this.porcentajeCumplimiento = porcentajeCumplimiento;
        this.tiempoPromedioRespuesta = tiempoPromedioRespuesta;
        this.tiempoPromedioEjecucion = tiempoPromedioEjecucion;
        this.tiempoPromedioTotal = tiempoPromedioTotal;
        this.porcentajeCompletado = porcentajeCompletado;
    }

    // Getters y Setters
    public Long getIdMensajero() {
        return idMensajero;
    }

    public void setIdMensajero(Long idMensajero) {
        this.idMensajero = idMensajero;
    }

    public String getNombreMensajero() {
        return nombreMensajero;
    }

    public void setNombreMensajero(String nombreMensajero) {
        this.nombreMensajero = nombreMensajero;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getTotalTareasAsignadas() {
        return totalTareasAsignadas;
    }

    public void setTotalTareasAsignadas(Integer totalTareasAsignadas) {
        this.totalTareasAsignadas = totalTareasAsignadas;
    }

    public Integer getTareasCompletadas() {
        return tareasCompletadas;
    }

    public void setTareasCompletadas(Integer tareasCompletadas) {
        this.tareasCompletadas = tareasCompletadas;
    }

    public Integer getTareasPendientes() {
        return tareasPendientes;
    }

    public void setTareasPendientes(Integer tareasPendientes) {
        this.tareasPendientes = tareasPendientes;
    }

    public Integer getTareasEnProceso() {
        return tareasEnProceso;
    }

    public void setTareasEnProceso(Integer tareasEnProceso) {
        this.tareasEnProceso = tareasEnProceso;
    }

    public Integer getEntregasATiempo() {
        return entregasATiempo;
    }

    public void setEntregasATiempo(Integer entregasATiempo) {
        this.entregasATiempo = entregasATiempo;
    }

    public Integer getEntregasTardias() {
        return entregasTardias;
    }

    public void setEntregasTardias(Integer entregasTardias) {
        this.entregasTardias = entregasTardias;
    }

    public Double getPorcentajeCumplimiento() {
        return porcentajeCumplimiento;
    }

    public void setPorcentajeCumplimiento(Double porcentajeCumplimiento) {
        this.porcentajeCumplimiento = porcentajeCumplimiento;
    }

    public Double getTiempoPromedioRespuesta() {
        return tiempoPromedioRespuesta;
    }

    public void setTiempoPromedioRespuesta(Double tiempoPromedioRespuesta) {
        this.tiempoPromedioRespuesta = tiempoPromedioRespuesta;
    }

    public Double getTiempoPromedioEjecucion() {
        return tiempoPromedioEjecucion;
    }

    public void setTiempoPromedioEjecucion(Double tiempoPromedioEjecucion) {
        this.tiempoPromedioEjecucion = tiempoPromedioEjecucion;
    }

    public Double getTiempoPromedioTotal() {
        return tiempoPromedioTotal;
    }

    public void setTiempoPromedioTotal(Double tiempoPromedioTotal) {
        this.tiempoPromedioTotal = tiempoPromedioTotal;
    }

    public Double getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public void setPorcentajeCompletado(Double porcentajeCompletado) {
        this.porcentajeCompletado = porcentajeCompletado;
    }
}
