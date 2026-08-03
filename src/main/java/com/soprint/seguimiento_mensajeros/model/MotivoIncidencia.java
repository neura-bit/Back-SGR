package com.soprint.seguimiento_mensajeros.model;

/**
 * Motivos por los que un mensajero puede reportar una incidencia al finalizar
 * una tarea. Se persisten como texto (EnumType.STRING) para que agregar valores
 * nuevos no altere los registros existentes.
 */
public enum MotivoIncidencia {

    DIRECCION_INCORRECTA("Dirección incorrecta o inexacta", true),
    SIN_REFERENCIA("No se añadió referencia", true),
    TELEFONO_INCORRECTO("Teléfono incorrecto", true),
    DATOS_INCOMPLETOS("Datos incompletos o mal ingresados", true),
    OTRO("Otro", false);

    private final String etiqueta;

    /**
     * Indica si el motivo corresponde a datos del cliente. De ello depende a
     * quién se atribuye la incidencia: al responsable de los datos del cliente
     * o al asesor que creó la tarea.
     */
    private final boolean datosDelCliente;

    MotivoIncidencia(String etiqueta, boolean datosDelCliente) {
        this.etiqueta = etiqueta;
        this.datosDelCliente = datosDelCliente;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean esDatosDelCliente() {
        return datosDelCliente;
    }

    /** true si el motivo exige que el mensajero escriba una descripción. */
    public boolean requiereDescripcion() {
        return this == OTRO;
    }
}
