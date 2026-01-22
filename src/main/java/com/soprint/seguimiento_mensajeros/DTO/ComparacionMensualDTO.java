package com.soprint.seguimiento_mensajeros.DTO;

import java.time.YearMonth;
import java.util.List;

/**
 * DTO para representar la comparación de rendimiento mensual de un mensajero.
 */
public class ComparacionMensualDTO {

    private Long idMensajero;
    private String nombreMensajero;

    // Período de comparación
    private YearMonth mesInicio;
    private YearMonth mesFin;

    // Métricas del primer mes (baseline)
    private Double porcentajeCumplimientoInicial;
    private Integer tareasCompletadasInicial;

    // Métricas del último mes (actual)
    private Double porcentajeCumplimientoFinal;
    private Integer tareasCompletadasFinal;

    // Cambio calculado (positivo = mejora, negativo = empeoramiento)
    private Double cambioRendimiento;
    // "MEJORA", "EMPEORAMIENTO", "SIN_CAMBIO"
    private String tendencia;

    // Detalle mes a mes
    private List<DetalleMes> detalleMeses;

    // Constructor vacío
    public ComparacionMensualDTO() {
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

    public List<DetalleMes> getDetalleMeses() {
        return detalleMeses;
    }

    public void setDetalleMeses(List<DetalleMes> detalleMeses) {
        this.detalleMeses = detalleMeses;
    }

    /**
     * Clase interna para representar el detalle de cada mes.
     */
    public static class DetalleMes {
        private YearMonth mes;
        private Double porcentajeCumplimiento;
        private Integer tareasCompletadas;
        private Double cambioVsMesAnterior;

        public DetalleMes() {
        }

        public DetalleMes(YearMonth mes, Double porcentajeCumplimiento, Integer tareasCompletadas,
                Double cambioVsMesAnterior) {
            this.mes = mes;
            this.porcentajeCumplimiento = porcentajeCumplimiento;
            this.tareasCompletadas = tareasCompletadas;
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

        public Double getCambioVsMesAnterior() {
            return cambioVsMesAnterior;
        }

        public void setCambioVsMesAnterior(Double cambioVsMesAnterior) {
            this.cambioVsMesAnterior = cambioVsMesAnterior;
        }
    }
}
