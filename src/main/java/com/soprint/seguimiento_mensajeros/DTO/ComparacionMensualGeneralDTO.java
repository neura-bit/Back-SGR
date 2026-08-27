package com.soprint.seguimiento_mensajeros.DTO;

import java.time.YearMonth;
import java.util.List;

/**
 * Comparación de rendimiento mensual de toda la operación, no de un mensajero.
 *
 * Es el equivalente agregado de {@link ComparacionMensualDTO}. Va aparte y no
 * reutiliza aquel porque el detalle mensual necesita datos que en la vista
 * individual no tienen sentido: cuántos mensajeros trabajaron ese mes y cuántas
 * tareas se asignaron en total. Sin eso, una caída del cumplimiento no se puede
 * interpretar: no es lo mismo con tres mensajeros que con doce.
 */
public class ComparacionMensualGeneralDTO {

    /** Sucursal sobre la que se calculó, o null si es toda la empresa. */
    private Long idSucursal;
    private String nombreSucursal;

    private YearMonth mesInicio;
    private YearMonth mesFin;

    private Double porcentajeCumplimientoInicial;
    private Integer tareasCompletadasInicial;

    private Double porcentajeCumplimientoFinal;
    private Integer tareasCompletadasFinal;

    /** Positivo = mejora, negativo = empeoramiento. */
    private Double cambioRendimiento;

    /** "MEJORA", "EMPEORAMIENTO", "SIN_CAMBIO" */
    private String tendencia;

    private List<DetalleMesGeneral> detalleMeses;

    public ComparacionMensualGeneralDTO() {
    }

    public static class DetalleMesGeneral {

        private YearMonth mes;
        private Double porcentajeCumplimiento;
        private Integer tareasCompletadas;
        private Integer totalTareasAsignadas;
        private Integer entregasATiempo;
        private Integer entregasTardias;

        /** Mensajeros con al menos una tarea asignada en el mes. */
        private Integer mensajerosActivos;

        private Double cambioVsMesAnterior;

        public DetalleMesGeneral() {
        }

        public DetalleMesGeneral(YearMonth mes, Double porcentajeCumplimiento, Integer tareasCompletadas,
                Integer totalTareasAsignadas, Integer entregasATiempo, Integer entregasTardias,
                Integer mensajerosActivos, Double cambioVsMesAnterior) {
            this.mes = mes;
            this.porcentajeCumplimiento = porcentajeCumplimiento;
            this.tareasCompletadas = tareasCompletadas;
            this.totalTareasAsignadas = totalTareasAsignadas;
            this.entregasATiempo = entregasATiempo;
            this.entregasTardias = entregasTardias;
            this.mensajerosActivos = mensajerosActivos;
            this.cambioVsMesAnterior = cambioVsMesAnterior;
        }

        public YearMonth getMes() {
            return mes;
        }

        public void setMes(YearMonth mes) {
            this.mes = mes;
        }

        public Double getPorcentajeCumplimiento() {
            return porcentajeCumplimiento;
        }

        public void setPorcentajeCumplimiento(Double porcentajeCumplimiento) {
            this.porcentajeCumplimiento = porcentajeCumplimiento;
        }

        public Integer getTareasCompletadas() {
            return tareasCompletadas;
        }

        public void setTareasCompletadas(Integer tareasCompletadas) {
            this.tareasCompletadas = tareasCompletadas;
        }

        public Integer getTotalTareasAsignadas() {
            return totalTareasAsignadas;
        }

        public void setTotalTareasAsignadas(Integer totalTareasAsignadas) {
            this.totalTareasAsignadas = totalTareasAsignadas;
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

        public Integer getMensajerosActivos() {
            return mensajerosActivos;
        }

        public void setMensajerosActivos(Integer mensajerosActivos) {
            this.mensajerosActivos = mensajerosActivos;
        }

        public Double getCambioVsMesAnterior() {
            return cambioVsMesAnterior;
        }

        public void setCambioVsMesAnterior(Double cambioVsMesAnterior) {
            this.cambioVsMesAnterior = cambioVsMesAnterior;
        }
    }

    public Long getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Long idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public void setNombreSucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
    }

    public YearMonth getMesInicio() {
        return mesInicio;
    }

    public void setMesInicio(YearMonth mesInicio) {
        this.mesInicio = mesInicio;
    }

    public YearMonth getMesFin() {
        return mesFin;
    }

    public void setMesFin(YearMonth mesFin) {
        this.mesFin = mesFin;
    }

    public Double getPorcentajeCumplimientoInicial() {
        return porcentajeCumplimientoInicial;
    }

    public void setPorcentajeCumplimientoInicial(Double porcentajeCumplimientoInicial) {
        this.porcentajeCumplimientoInicial = porcentajeCumplimientoInicial;
    }

    public Integer getTareasCompletadasInicial() {
        return tareasCompletadasInicial;
    }

    public void setTareasCompletadasInicial(Integer tareasCompletadasInicial) {
        this.tareasCompletadasInicial = tareasCompletadasInicial;
    }

    public Double getPorcentajeCumplimientoFinal() {
        return porcentajeCumplimientoFinal;
    }

    public void setPorcentajeCumplimientoFinal(Double porcentajeCumplimientoFinal) {
        this.porcentajeCumplimientoFinal = porcentajeCumplimientoFinal;
    }

    public Integer getTareasCompletadasFinal() {
        return tareasCompletadasFinal;
    }

    public void setTareasCompletadasFinal(Integer tareasCompletadasFinal) {
        this.tareasCompletadasFinal = tareasCompletadasFinal;
    }

    public Double getCambioRendimiento() {
        return cambioRendimiento;
    }

    public void setCambioRendimiento(Double cambioRendimiento) {
        this.cambioRendimiento = cambioRendimiento;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public List<DetalleMesGeneral> getDetalleMeses() {
        return detalleMeses;
    }

    public void setDetalleMeses(List<DetalleMesGeneral> detalleMeses) {
        this.detalleMeses = detalleMeses;
    }
}
