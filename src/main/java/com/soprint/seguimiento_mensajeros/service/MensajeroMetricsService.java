package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.MensajeroMetricsDTO;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para calcular métricas de rendimiento de mensajeros.
 */
@Service
@Transactional(readOnly = true)
public class MensajeroMetricsService {

        private final TareaRepository tareaRepository;
        private final UsuarioRepository usuarioRepository;

        public MensajeroMetricsService(TareaRepository tareaRepository, UsuarioRepository usuarioRepository) {
                this.tareaRepository = tareaRepository;
                this.usuarioRepository = usuarioRepository;
        }

        /**
         * Obtiene las métricas de un mensajero específico en un rango de fechas.
         */
        public MensajeroMetricsDTO getMetricasMensajero(Long idMensajero, LocalDate fechaInicio, LocalDate fechaFin) {
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
                List<MensajeroMetricsDTO> metricas = new ArrayList<>();
                for (Usuario mensajero : mensajeros) {
                        try {
                                MensajeroMetricsDTO dto = getMetricasMensajero(mensajero.getIdUsuario(), fechaInicio,
                                                fechaFin);
                                metricas.add(dto);
                        } catch (Exception e) {
                                // Si hay error con un mensajero específico, continuar con los demás
                                System.err.println(
                                                "Error obteniendo métricas para mensajero " + mensajero.getIdUsuario()
                                                                + ": " + e.getMessage());
                        }
                }

                // Ordenar por porcentaje de cumplimiento descendente
                metricas.sort((a, b) -> Double.compare(
                                b.getPorcentajeCumplimiento() != null ? b.getPorcentajeCumplimiento() : 0,
                                a.getPorcentajeCumplimiento() != null ? a.getPorcentajeCumplimiento() : 0));

                return metricas;
        }

        /**
         * Obtiene las métricas de un mensajero para un día específico.
         */
        public MensajeroMetricsDTO getResumenDiario(Long idMensajero, LocalDate fecha) {
                return getMetricasMensajero(idMensajero, fecha, fecha);
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
}
