package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;

@Entity
public class TipoOperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_operacion")
    private Long idTipoOperacion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Los tipos de operacion no se pueden borrar: hay tareas que ya los usan y
     * el borrado fisico rompe la clave foranea. Se desactivan, y asi dejan de
     * ofrecerse al crear tareas nuevas sin tocar el historial.
     *
     * Sin NOT NULL: agregar una columna obligatoria a una tabla con filas
     * existentes falla en PostgreSQL. El servicio normaliza el valor.
     */
    /*
     * Sin inicializar a true a proposito. Si el campo naciera con valor,
     * Jackson nunca lo dejaria en null al deserializar el cuerpo de un PUT, y
     * el servicio no podria distinguir "no me lo mandaron" de "quieren
     * activarlo": editar el nombre reactivaria un tipo dado de baja.
     * create() le pone true de forma explicita.
     */
    @Column(name = "activo", columnDefinition = "boolean default true")
    private Boolean activo;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getIdTipoOperacion() {
        return idTipoOperacion;
    }

    public void setIdTipoOperacion(Long idTipoOperacion) {
        this.idTipoOperacion = idTipoOperacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoOperacion(Long idTipoOperacion, String nombre) {
        this.idTipoOperacion = idTipoOperacion;
        this.nombre = nombre;
    }

    public TipoOperacion() {
    }
}
