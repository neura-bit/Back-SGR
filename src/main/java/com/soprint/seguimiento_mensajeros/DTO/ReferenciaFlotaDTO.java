package com.soprint.seguimiento_mensajeros.DTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que logro la flota en cada area y tipo de operacion en un periodo y
 * alcance. Es la base contra la que se compara a cada mensajero, y se expone
 * para que la evaluacion se pueda auditar.
 */
public class ReferenciaFlotaDTO {

    private Long idSucursal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    /** % a tiempo de toda la flota, sin distinguir trabajo. */
    private Double porcentajeGeneral;
    private Integer minTareasFlota;
    private Integer minTareasMensajero;
    private List<Area> areas;
    private List<Tipo> tipos;

    public record Area(String area, String etiqueta, Integer tareas, Integer mensajeros, Double porcentajeATiempo,
            Double minutosEjecucionPromedio) {
    }

    /**
     * suficiente = false: al menos un mensajero no tiene suficientes tareas de
     * otros en este tipo y se le compara contra su area.
     */
    public record Tipo(Long idTipoOperacion, String nombre, String area, Integer tareas, Integer mensajeros,
            Double porcentajeATiempo, Double minutosEjecucionPromedio, Boolean suficiente,
            List<Vehiculo> vehiculos) {
    }

    /** suficiente = false: alguien con ese vehiculo se compara contra el tipo completo. */
    public record Vehiculo(String vehiculo, String etiqueta, Integer tareas, Double porcentajeATiempo,
            Boolean suficiente) {
    }

    public Long getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Long idSucursal) {
        this.idSucursal = idSucursal;
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

    public Double getPorcentajeGeneral() {
        return porcentajeGeneral;
    }

    public void setPorcentajeGeneral(Double porcentajeGeneral) {
        this.porcentajeGeneral = porcentajeGeneral;
    }

    public Integer getMinTareasFlota() {
        return minTareasFlota;
    }

    public void setMinTareasFlota(Integer minTareasFlota) {
        this.minTareasFlota = minTareasFlota;
    }

    public Integer getMinTareasMensajero() {
        return minTareasMensajero;
    }

    public void setMinTareasMensajero(Integer minTareasMensajero) {
        this.minTareasMensajero = minTareasMensajero;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    public List<Tipo> getTipos() {
        return tipos;
    }

    public void setTipos(List<Tipo> tipos) {
        this.tipos = tipos;
    }
}
