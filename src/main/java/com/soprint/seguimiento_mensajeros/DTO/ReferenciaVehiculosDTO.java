package com.soprint.seguimiento_mensajeros.DTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que logro la flota con cada vehiculo en un periodo y alcance. Es la base
 * contra la que se compara a cada mensajero, y se expone para que la
 * evaluacion se pueda auditar.
 */
public class ReferenciaVehiculosDTO {

    private Long idSucursal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    /** % a tiempo de toda la flota, sin distinguir vehiculo. */
    private Double porcentajeGeneral;
    private Integer minTareasFlota;
    private Integer minTareasMensajero;
    private List<Fila> vehiculos;

    public static class Fila {
        private String vehiculo;
        private String etiqueta;
        private Integer tareas;
        private Integer mensajeros;
        private Double porcentajeATiempo;
        private Double minutosEjecucionPromedio;
        /** false = "Pocas tareas": nadie con ese vehiculo recibe indice. */
        private Boolean suficiente;

        public Fila(String vehiculo, String etiqueta, Integer tareas, Integer mensajeros, Double porcentajeATiempo,
                Double minutosEjecucionPromedio, Boolean suficiente) {
            this.vehiculo = vehiculo;
            this.etiqueta = etiqueta;
            this.tareas = tareas;
            this.mensajeros = mensajeros;
            this.porcentajeATiempo = porcentajeATiempo;
            this.minutosEjecucionPromedio = minutosEjecucionPromedio;
            this.suficiente = suficiente;
        }

        public String getVehiculo() {
            return vehiculo;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public Integer getTareas() {
            return tareas;
        }

        public Integer getMensajeros() {
            return mensajeros;
        }

        public Double getPorcentajeATiempo() {
            return porcentajeATiempo;
        }

        public Double getMinutosEjecucionPromedio() {
            return minutosEjecucionPromedio;
        }

        public Boolean getSuficiente() {
            return suficiente;
        }
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

    public List<Fila> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Fila> vehiculos) {
        this.vehiculos = vehiculos;
    }
}
