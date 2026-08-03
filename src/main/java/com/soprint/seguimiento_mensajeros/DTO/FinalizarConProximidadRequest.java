package com.soprint.seguimiento_mensajeros.DTO;

public class FinalizarConProximidadRequest {
    private Double latitudMensajero;
    private Double longitudMensajero;
    private Long idEstadoTarea;
    private String observacion;
    // Vehículo usado para realizar la tarea
    private com.soprint.seguimiento_mensajeros.model.TipoVehiculo tipoVehiculo;

    public Double getLatitudMensajero() {
        return latitudMensajero;
    }

    public void setLatitudMensajero(Double latitudMensajero) {
        this.latitudMensajero = latitudMensajero;
    }

    public Double getLongitudMensajero() {
        return longitudMensajero;
    }

    public void setLongitudMensajero(Double longitudMensajero) {
        this.longitudMensajero = longitudMensajero;
    }

    public Long getIdEstadoTarea() {
        return idEstadoTarea;
    }

    public void setIdEstadoTarea(Long idEstadoTarea) {
        this.idEstadoTarea = idEstadoTarea;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public com.soprint.seguimiento_mensajeros.model.TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(com.soprint.seguimiento_mensajeros.model.TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }
}
