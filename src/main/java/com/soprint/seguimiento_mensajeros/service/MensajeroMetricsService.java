package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.ComparacionMensualDTO;
import com.soprint.seguimiento_mensajeros.DTO.ComparacionMensualGeneralDTO;
import com.soprint.seguimiento_mensajeros.DTO.MensajeroMetricsDTO;
import com.soprint.seguimiento_mensajeros.DTO.ReferenciaVehiculosDTO;
import com.soprint.seguimiento_mensajeros.model.TipoVehiculo;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.SucursalRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para calcular métricas de rendimiento de mensajeros.
 */
@Service
@Transactional(readOnly = true)
public class MensajeroMetricsService {

        private final TareaRepository tareaRepository;
        private final UsuarioRepository usuarioRepository;
        private final SucursalRepository sucursalRepository;

        public MensajeroMetricsService(TareaRepository tareaRepository, UsuarioRepository usuarioRepository,
                        SucursalRepository sucursalRepository) {
                this.tareaRepository = tareaRepository;
                this.usuarioRepository = usuarioRepository;
                this.sucursalRepository = sucursalRepository;
        }

        /**
         * Métricas de un mensajero, evaluado contra la flota de toda la empresa
         * que usó sus mismos vehículos.
         */
        public MensajeroMetricsDTO getMetricasMensajero(Long idMensajero, LocalDate fechaInicio, LocalDate fechaFin) {
                MensajeroMetricsDTO dto = getMetricasCrudas(idMensajero, fechaInicio, fechaFin);
                aplicarAjusteVehiculo(List.of(dto), usuarioRepository.findByRolNombre("MENSAJERO"), fechaInicio,
                                fechaFin);
                return dto;
        }

        /**
         * Igual que {@link #getMetricasMensajero}, pero la flota de referencia es
         * solo la de la sucursal, para que coincida con el comparativo de esa
         * sucursal.
         */
        public MensajeroMetricsDTO getMetricasMensajeroEnSucursal(Long idSucursal, Long idMensajero,
                        LocalDate fechaInicio, LocalDate fechaFin) {
                MensajeroMetricsDTO dto = getMetricasCrudas(idMensajero, fechaInicio, fechaFin);
                aplicarAjusteVehiculo(List.of(dto),
                                usuarioRepository.findByRolNombreAndSucursalIdSucursal("MENSAJERO", idSucursal),
                                fechaInicio, fechaFin);
                return dto;
        }

        /**
         * Métricas sin el ajuste por vehículo. Las usan las comparativas
         * mensuales y el resumen diario, que solo miran el cumplimiento crudo.
         */
        private MensajeroMetricsDTO getMetricasCrudas(Long idMensajero, LocalDate fechaInicio, LocalDate fechaFin) {
                // Convertir LocalDate a LocalDateTime para las queries
                LocalDateTime inicio = fechaInicio.atStartOfDay();
                LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

                // Obtener información del mensajero
                Usuario mensajero = usuarioRepository.findById(idMensajero)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Mensajero no encontrado con id: " + idMensajero));

                // Obtener todas las tareas del mensajero en el rango
                var tareas = tareaRepository.findByMensajeroAsignadoIdUsuarioAndFechaCreacionBetween(
                                idMensajero, inicio, fin);

                // Calcular contadores
                int totalTareasAsignadas = tareas.size();
                int tareasCompletadas = (int) tareas.stream()
                                .filter(t -> t.getEstadoTarea() != null
                                                && "COMPLETADA".equals(t.getEstadoTarea().getNombre()))
                                .count();
                int tareasPendientes = (int) tareas.stream()
                                .filter(t -> t.getEstadoTarea() != null
                                                && "PENDIENTE".equals(t.getEstadoTarea().getNombre()))
                                .count();
                int tareasEnProceso = (int) tareas.stream()
                                .filter(t -> t.getEstadoTarea() != null
                                                && "EN PROCESO".equals(t.getEstadoTarea().getNombre()))
                                .count();

                // Calcular entregas a tiempo
                Long entregasATiempoCount = tareaRepository.countByMensajeroAndEntregaATiempoAndFechas(
                                idMensajero, true, inicio, fin);
                Long entregasTardiasCount = tareaRepository.countByMensajeroAndEntregaATiempoAndFechas(
                                idMensajero, false, inicio, fin);
                int entregasATiempo = entregasATiempoCount != null ? entregasATiempoCount.intValue() : 0;
                int entregasTardias = entregasTardiasCount != null ? entregasTardiasCount.intValue() : 0;

                // Calcular porcentaje de cumplimiento
                int totalEntregas = entregasATiempo + entregasTardias;
                double porcentajeCumplimiento = totalEntregas > 0
                                ? (entregasATiempo * 100.0) / totalEntregas
                                : 0.0;

                // Calcular porcentaje de completado
                double porcentajeCompletado = totalTareasAsignadas > 0
                                ? (tareasCompletadas * 100.0) / totalTareasAsignadas
                                : 0.0;

                // Obtener promedios de tiempos
                Double tiempoPromedioRespuesta = tareaRepository.avgTiempoRespuestaByMensajeroAndFechas(
                                idMensajero, inicio, fin);
                Double tiempoPromedioEjecucion = tareaRepository.avgTiempoEjecucionByMensajeroAndFechas(
                                idMensajero, inicio, fin);
                Double tiempoPromedioTotal = tareaRepository.avgTiempoTotalByMensajeroAndFechas(
                                idMensajero, inicio, fin);

                // Construir y retornar el DTO
                MensajeroMetricsDTO dto = new MensajeroMetricsDTO();
                dto.setIdMensajero(idMensajero);
                dto.setNombreMensajero(mensajero.getNombre());
                dto.setFechaInicio(fechaInicio);
                dto.setFechaFin(fechaFin);
                dto.setTotalTareasAsignadas(totalTareasAsignadas);
                dto.setTareasCompletadas(tareasCompletadas);
                dto.setTareasPendientes(tareasPendientes);
                dto.setTareasEnProceso(tareasEnProceso);
                dto.setEntregasATiempo(entregasATiempo);
                dto.setEntregasTardias(entregasTardias);
                dto.setPorcentajeCumplimiento(Math.round(porcentajeCumplimiento * 100.0) / 100.0);
                dto.setPorcentajeCompletado(Math.round(porcentajeCompletado * 100.0) / 100.0);
                dto.setTiempoPromedioRespuesta(tiempoPromedioRespuesta != null
                                ? Math.round(tiempoPromedioRespuesta * 100.0) / 100.0
                                : null);
                dto.setTiempoPromedioEjecucion(tiempoPromedioEjecucion != null
                                ? Math.round(tiempoPromedioEjecucion * 100.0) / 100.0
                                : null);
                dto.setTiempoPromedioTotal(tiempoPromedioTotal != null
                                ? Math.round(tiempoPromedioTotal * 100.0) / 100.0
                                : null);

                return dto;
        }

        /**
         * Obtiene las métricas de todos los mensajeros en un rango de fechas.
         * Útil para comparativas de rendimiento.
         */
        public List<MensajeroMetricsDTO> getComparativoMensajeros(LocalDate fechaInicio, LocalDate fechaFin) {
                // Obtener todos los usuarios con rol MENSAJERO
                List<Usuario> mensajeros = usuarioRepository.findByRolNombre("MENSAJERO");

                return calcularMetricasParaMensajeros(mensajeros, fechaInicio, fechaFin);
        }

        /**
         * Obtiene las métricas de todos los mensajeros de una sucursal específica.
         * Útil para comparativas de rendimiento por sucursal.
         */
        public List<MensajeroMetricsDTO> getComparativoMensajerosPorSucursal(Long idSucursal, LocalDate fechaInicio,
                        LocalDate fechaFin) {
                // Obtener todos los usuarios con rol MENSAJERO de la sucursal especificada
                List<Usuario> mensajeros = usuarioRepository.findByRolNombreAndSucursalIdSucursal("MENSAJERO",
                                idSucursal);

                return calcularMetricasParaMensajeros(mensajeros, fechaInicio, fechaFin);
        }

        /**
         * Método privado para calcular métricas de una lista de mensajeros.
         */
        private List<MensajeroMetricsDTO> calcularMetricasParaMensajeros(List<Usuario> mensajeros,
                        LocalDate fechaInicio, LocalDate fechaFin) {
                List<MensajeroMetricsDTO> metricas = calcularMetricasCrudas(mensajeros, fechaInicio, fechaFin);
                aplicarAjusteVehiculo(metricas, mensajeros, fechaInicio, fechaFin);

                // El ranking va por el cumplimiento ajustado por vehículo. Quien no
                // tiene base suficiente queda al final, ordenado por el crudo.
                metricas.sort(Comparator
                                .comparing(MensajeroMetricsDTO::getCumplimientoAjustado,
                                                Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(m -> m.getPorcentajeCumplimiento() != null
                                                ? m.getPorcentajeCumplimiento()
                                                : 0.0, Comparator.reverseOrder()));

                return metricas;
        }

        private List<MensajeroMetricsDTO> calcularMetricasCrudas(List<Usuario> mensajeros,
                        LocalDate fechaInicio, LocalDate fechaFin) {
                List<MensajeroMetricsDTO> metricas = new ArrayList<>();
                for (Usuario mensajero : mensajeros) {
                        try {
                                MensajeroMetricsDTO dto = getMetricasCrudas(mensajero.getIdUsuario(), fechaInicio,
                                                fechaFin);
                                metricas.add(dto);
                        } catch (Exception e) {
                                // Si hay error con un mensajero específico, continuar con los demás
                                System.err.println(
                                                "Error obteniendo métricas para mensajero " + mensajero.getIdUsuario()
                                                                + ": " + e.getMessage());
                        }
                }
                return metricas;
        }

        /**
         * Construye la referencia por vehículo con las tareas finalizadas de la
         * flota del alcance.
         */
        private BaselineVehiculos construirBaseline(List<Usuario> flota, LocalDate fechaInicio, LocalDate fechaFin) {
                List<Long> ids = flota.stream().map(Usuario::getIdUsuario).toList();
                if (ids.isEmpty()) {
                        return new BaselineVehiculos(List.of());
                }
                List<BaselineVehiculos.Registro> registros = tareaRepository
                                .findDatosVehiculoByMensajerosAndFechas(ids, fechaInicio.atStartOfDay(),
                                                fechaFin.atTime(LocalTime.MAX))
                                .stream()
                                .map(fila -> new BaselineVehiculos.Registro(
                                                (Long) fila[0],
                                                (TipoVehiculo) fila[1],
                                                (Boolean) fila[2],
                                                (Long) fila[3]))
                                .toList();
                return new BaselineVehiculos(registros);
        }

        /** Completa en cada DTO los campos de la evaluación por vehículo. */
        private void aplicarAjusteVehiculo(List<MensajeroMetricsDTO> metricas, List<Usuario> flota,
                        LocalDate fechaInicio, LocalDate fechaFin) {
                BaselineVehiculos baseline = construirBaseline(flota, fechaInicio, fechaFin);
                for (MensajeroMetricsDTO dto : metricas) {
                        BaselineVehiculos.Evaluacion evaluacion = baseline.evaluar(dto.getIdMensajero());
                        dto.setTareasEvaluadasVehiculo(evaluacion.tareasEvaluadas());
                        dto.setCumplimientoEsperado(evaluacion.cumplimientoEsperado());
                        dto.setDiferenciaVsPares(evaluacion.diferenciaVsPares());
                        dto.setCumplimientoAjustado(evaluacion.cumplimientoAjustado());
                        dto.setIndiceEficiencia(evaluacion.indiceEficiencia());

                        Map<String, Integer> porVehiculo = new LinkedHashMap<>();
                        evaluacion.tareasPorVehiculo().forEach((v, n) -> porVehiculo.put(v.getEtiqueta(), n));
                        dto.setTareasPorVehiculo(porVehiculo);
                }
        }

        /**
         * Tabla "Referencia de la flota por vehículo": lo que logró la flota con
         * cada vehículo. Sin idSucursal es toda la empresa.
         */
        public ReferenciaVehiculosDTO getReferenciaVehiculos(Long idSucursal, LocalDate fechaInicio,
                        LocalDate fechaFin) {
                List<Usuario> flota = idSucursal != null
                                ? usuarioRepository.findByRolNombreAndSucursalIdSucursal("MENSAJERO", idSucursal)
                                : usuarioRepository.findByRolNombre("MENSAJERO");
                BaselineVehiculos baseline = construirBaseline(flota, fechaInicio, fechaFin);

                ReferenciaVehiculosDTO dto = new ReferenciaVehiculosDTO();
                dto.setIdSucursal(idSucursal);
                dto.setFechaInicio(fechaInicio);
                dto.setFechaFin(fechaFin);
                dto.setPorcentajeGeneral(baseline.getPorcentajeGeneral());
                dto.setMinTareasFlota(BaselineVehiculos.MIN_TAREAS_FLOTA);
                dto.setMinTareasMensajero(BaselineVehiculos.MIN_TAREAS_MENSAJERO);
                dto.setVehiculos(baseline.referencias().stream()
                                .map(r -> new ReferenciaVehiculosDTO.Fila(
                                                r.vehiculo().name(),
                                                r.vehiculo().getEtiqueta(),
                                                r.tareas(),
                                                r.mensajeros(),
                                                r.porcentajeATiempo(),
                                                r.minutosEjecucionPromedio(),
                                                r.suficiente()))
                                .toList());
                return dto;
        }

        /**
         * Obtiene las métricas de un mensajero para un día específico.
         */
        public MensajeroMetricsDTO getResumenDiario(Long idMensajero, LocalDate fecha) {
                return getMetricasCrudas(idMensajero, fecha, fecha);
        }

        /**
         * Valida que un mensajero pertenezca a una sucursal específica.
         */
        public boolean mensajeroPerteneceASucursal(Long idMensajero, Long idSucursal) {
                Usuario mensajero = usuarioRepository.findById(idMensajero).orElse(null);
                return mensajero != null
                                && mensajero.getSucursal() != null
                                && mensajero.getSucursal().getIdSucursal().equals(idSucursal);
        }

        /**
         * Compara el rendimiento de un mensajero entre un rango de meses.
         * 
         * @param idMensajero ID del mensajero
         * @param mesInicio   Mes de inicio (ej: 2024-01)
         * @param mesFin      Mes de fin (ej: 2024-03)
         * @return DTO con métricas de comparación y detalle mes a mes
         */
        public ComparacionMensualDTO getComparacionMensual(Long idMensajero, YearMonth mesInicio, YearMonth mesFin) {
                // Validar que mesInicio <= mesFin
                if (mesInicio.isAfter(mesFin)) {
                        throw new IllegalArgumentException("El mes de inicio no puede ser posterior al mes de fin");
                }

                // Obtener información del mensajero
                Usuario mensajero = usuarioRepository.findById(idMensajero)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Mensajero no encontrado con id: " + idMensajero));

                // Calcular métricas para cada mes en el rango
                List<ComparacionMensualDTO.DetalleMes> detalleMeses = new ArrayList<>();
                YearMonth mesActual = mesInicio;
                Double cumplimientoAnterior = null;

                while (!mesActual.isAfter(mesFin)) {
                        LocalDate primerDiaMes = mesActual.atDay(1);
                        LocalDate ultimoDiaMes = mesActual.atEndOfMonth();

                        // Reutilizar el método existente para obtener métricas del mes
                        MensajeroMetricsDTO metricasMes = getMetricasCrudas(idMensajero, primerDiaMes, ultimoDiaMes);

                        // Calcular cambio vs mes anterior
                        Double cambioVsAnterior = null;
                        if (cumplimientoAnterior != null && metricasMes.getPorcentajeCumplimiento() != null) {
                                cambioVsAnterior = Math.round(
                                                (metricasMes.getPorcentajeCumplimiento() - cumplimientoAnterior)
                                                                * 100.0)
                                                / 100.0;
                        }

                        ComparacionMensualDTO.DetalleMes detalle = new ComparacionMensualDTO.DetalleMes(
                                        mesActual,
                                        metricasMes.getPorcentajeCumplimiento(),
                                        metricasMes.getTareasCompletadas(),
                                        cambioVsAnterior);
                        detalleMeses.add(detalle);

                        cumplimientoAnterior = metricasMes.getPorcentajeCumplimiento();
                        mesActual = mesActual.plusMonths(1);
                }

                // Obtener métricas del primer y último mes
                ComparacionMensualDTO.DetalleMes primerMes = detalleMeses.get(0);
                ComparacionMensualDTO.DetalleMes ultimoMes = detalleMeses.get(detalleMeses.size() - 1);

                // Calcular cambio total
                Double cambioRendimiento = 0.0;
                String tendencia = "SIN_CAMBIO";

                if (primerMes.getPorcentajeCumplimiento() != null && ultimoMes.getPorcentajeCumplimiento() != null) {
                        cambioRendimiento = Math.round(
                                        (ultimoMes.getPorcentajeCumplimiento() - primerMes.getPorcentajeCumplimiento())
                                                        * 100.0)
                                        / 100.0;

                        if (cambioRendimiento > 0) {
                                tendencia = "MEJORA";
                        } else if (cambioRendimiento < 0) {
                                tendencia = "EMPEORAMIENTO";
                        }
                }

                // Construir DTO de respuesta
                ComparacionMensualDTO dto = new ComparacionMensualDTO();
                dto.setIdMensajero(idMensajero);
                dto.setNombreMensajero(mensajero.getNombre());
                dto.setMesInicio(mesInicio);
                dto.setMesFin(mesFin);
                dto.setPorcentajeCumplimientoInicial(primerMes.getPorcentajeCumplimiento());
                dto.setTareasCompletadasInicial(primerMes.getTareasCompletadas());
                dto.setPorcentajeCumplimientoFinal(ultimoMes.getPorcentajeCumplimiento());
                dto.setTareasCompletadasFinal(ultimoMes.getTareasCompletadas());
                dto.setCambioRendimiento(cambioRendimiento);
                dto.setTendencia(tendencia);
                dto.setDetalleMeses(detalleMeses);

                return dto;
        }

        /**
         * Compara el rendimiento de toda la operacion entre un rango de meses.
         *
         * Es el equivalente agregado de {@link #getComparacionMensual}. La
         * diferencia que importa esta en como se agrega el cumplimiento: se suman
         * las entregas a tiempo y las tardias de todos los mensajeros y recien
         * ahi se divide. Promediar los porcentajes individuales daria otro
         * numero, porque le daria el mismo peso al mensajero que hizo tres
         * entregas que al que hizo cincuenta.
         *
         * @param idSucursal sucursal a medir, o null para toda la empresa
         */
        public ComparacionMensualGeneralDTO getComparacionMensualGeneral(Long idSucursal, YearMonth mesInicio,
                        YearMonth mesFin) {
                if (mesInicio.isAfter(mesFin)) {
                        throw new IllegalArgumentException("El mes de inicio no puede ser posterior al mes de fin");
                }

                List<ComparacionMensualGeneralDTO.DetalleMesGeneral> detalleMeses = new ArrayList<>();
                YearMonth mesActual = mesInicio;
                Double cumplimientoAnterior = null;

                while (!mesActual.isAfter(mesFin)) {
                        LocalDate primerDiaMes = mesActual.atDay(1);
                        LocalDate ultimoDiaMes = mesActual.atEndOfMonth();

                        List<Usuario> mensajerosDelAlcance = idSucursal != null
                                        ? usuarioRepository.findByRolNombreAndSucursalIdSucursal("MENSAJERO",
                                                        idSucursal)
                                        : usuarioRepository.findByRolNombre("MENSAJERO");
                        List<MensajeroMetricsDTO> metricasDelMes = calcularMetricasCrudas(mensajerosDelAlcance,
                                        primerDiaMes, ultimoDiaMes);

                        int entregasATiempo = 0;
                        int entregasTardias = 0;
                        int tareasCompletadas = 0;
                        int totalTareasAsignadas = 0;
                        int mensajerosActivos = 0;

                        for (MensajeroMetricsDTO metricas : metricasDelMes) {
                                entregasATiempo += valor(metricas.getEntregasATiempo());
                                entregasTardias += valor(metricas.getEntregasTardias());
                                tareasCompletadas += valor(metricas.getTareasCompletadas());

                                int asignadas = valor(metricas.getTotalTareasAsignadas());
                                totalTareasAsignadas += asignadas;

                                // Los mensajeros sin tareas ese mes no cuentan como activos:
                                // incluirlos haria parecer que el equipo crecio o se achico
                                // cuando en realidad solo cambio quien estuvo de licencia.
                                if (asignadas > 0) {
                                        mensajerosActivos++;
                                }
                        }

                        int totalEntregas = entregasATiempo + entregasTardias;
                        Double porcentajeCumplimiento = totalEntregas > 0
                                        ? Math.round((entregasATiempo * 100.0 / totalEntregas) * 100.0) / 100.0
                                        : 0.0;

                        Double cambioVsAnterior = null;
                        if (cumplimientoAnterior != null) {
                                cambioVsAnterior = Math.round(
                                                (porcentajeCumplimiento - cumplimientoAnterior) * 100.0) / 100.0;
                        }

                        detalleMeses.add(new ComparacionMensualGeneralDTO.DetalleMesGeneral(
                                        mesActual,
                                        porcentajeCumplimiento,
                                        tareasCompletadas,
                                        totalTareasAsignadas,
                                        entregasATiempo,
                                        entregasTardias,
                                        mensajerosActivos,
                                        cambioVsAnterior));

                        cumplimientoAnterior = porcentajeCumplimiento;
                        mesActual = mesActual.plusMonths(1);
                }

                ComparacionMensualGeneralDTO.DetalleMesGeneral primerMes = detalleMeses.get(0);
                ComparacionMensualGeneralDTO.DetalleMesGeneral ultimoMes = detalleMeses.get(detalleMeses.size() - 1);

                double cambioRendimiento = Math.round(
                                (ultimoMes.getPorcentajeCumplimiento() - primerMes.getPorcentajeCumplimiento())
                                                * 100.0)
                                / 100.0;

                String tendencia = "SIN_CAMBIO";
                if (cambioRendimiento > 0) {
                        tendencia = "MEJORA";
                } else if (cambioRendimiento < 0) {
                        tendencia = "EMPEORAMIENTO";
                }

                ComparacionMensualGeneralDTO dto = new ComparacionMensualGeneralDTO();
                dto.setIdSucursal(idSucursal);
                dto.setNombreSucursal(idSucursal != null
                                ? sucursalRepository.findById(idSucursal)
                                                .map(sucursal -> sucursal.nombre)
                                                .orElse("Sucursal desconocida")
                                : "Todas las sucursales");
                dto.setMesInicio(mesInicio);
                dto.setMesFin(mesFin);
                dto.setPorcentajeCumplimientoInicial(primerMes.getPorcentajeCumplimiento());
                dto.setTareasCompletadasInicial(primerMes.getTareasCompletadas());
                dto.setPorcentajeCumplimientoFinal(ultimoMes.getPorcentajeCumplimiento());
                dto.setTareasCompletadasFinal(ultimoMes.getTareasCompletadas());
                dto.setCambioRendimiento(cambioRendimiento);
                dto.setTendencia(tendencia);
                dto.setDetalleMeses(detalleMeses);

                return dto;
        }

        /** Evita repetir el chequeo de null en cada acumulador. */
        private int valor(Integer numero) {
                return numero != null ? numero : 0;
        }
}
