package com.soprint.seguimiento_mensajeros.model;

/**
 * Ciclo de vida de una incidencia de tarea.
 *
 * Hoy la web solo abre incidencias: la alerta deja de mostrarse cuando la tarea
 * termina, no cuando alguien la resuelve. RESUELTA queda previsto en el modelo
 * porque el día que se quiera un botón de "ya lo solucioné" el cambio es solo
 * de interfaz, sin migración ni reproceso del histórico.
 *
 * Al agregar valores, ver la nota sobre el CHECK en {@link MotivoIncidenciaTarea}.
 */
public enum EstadoIncidenciaTarea {

    ABIERTA("Abierta"),
    RESUELTA("Resuelta");

    private final String etiqueta;

    EstadoIncidenciaTarea(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
