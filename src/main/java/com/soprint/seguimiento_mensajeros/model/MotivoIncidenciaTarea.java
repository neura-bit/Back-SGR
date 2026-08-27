package com.soprint.seguimiento_mensajeros.model;

/**
 * Motivos por los que un supervisor puede reportar una incidencia sobre una
 * tarea mientras arma la ruta, antes de que salga el mensajero.
 *
 * Hoy hay uno solo, pero el catálogo está pensado para crecer.
 *
 * IMPORTANTE al agregar un valor nuevo: la columna se genera con
 * @Enumerated(EnumType.STRING) y Hibernate le crea un CHECK con los valores que
 * existían al momento de crear la tabla. Con ddl-auto=update ese CHECK NUNCA se
 * reescribe, así que sumar una constante acá compila y falla recién en runtime
 * con un 23514. Hay que correr a mano, en cada ambiente:
 *
 *   ALTER TABLE incidencia_tarea DROP CONSTRAINT incidencia_tarea_motivo_check;
 *   ALTER TABLE incidencia_tarea ADD CONSTRAINT incidencia_tarea_motivo_check
 *       CHECK (motivo IN ('FALTA_FACTURA', ...los nuevos...));
 */
public enum MotivoIncidenciaTarea {

    FALTA_FACTURA("Falta la factura física");

    private final String etiqueta;

    MotivoIncidenciaTarea(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
