package com.soprint.seguimiento_mensajeros.model;

/**
 * Vehículo con el que el mensajero realizó la tarea. Lo elige al finalizarla.
 * Se persiste como texto para que agregar valores nuevos no altere los
 * registros existentes.
 */
public enum TipoVehiculo {

    MOTO("Moto"),
    AUTO("Auto"),
    CAMIONETA("Camioneta"),
    CAMION("Camión");

    private final String etiqueta;

    TipoVehiculo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
