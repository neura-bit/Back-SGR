package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.TareaWebhookPayload;
import com.soprint.seguimiento_mensajeros.DTO.TareaFinalizadaWebhookPayload;
import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.ClienteRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TareaService implements ITareaService {

    private final TareaRepository tareaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final WebhookService webhookService;

    public TareaService(TareaRepository tareaRepository, ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository, WebhookService webhookService) {
        this.tareaRepository = tareaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.webhookService = webhookService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> findAll() {
        return tareaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tarea> findById(Long id) {
        return tareaRepository.findById(id);
    }

    @Override
    public Tarea create(Tarea tarea) {
        tarea.setIdTarea(null); // que el ID lo genere la BD
        tarea.setFechaCreacion(LocalDateTime.now()); // fecha de creación automática

        // Autogenerar código de 4 dígitos
        String nuevoCodigo = generarNuevoCodigo();
        tarea.setCodigo(nuevoCodigo);

        // aquí podrías poner un estado inicial por defecto si quieres
        // tarea.setEstadoTarea(estadoInicial);

        Tarea tareaGuardada = tareaRepository.save(tarea);

        // Enviar webhook con información de la tarea y cliente
        enviarWebhookTareaCreada(tareaGuardada);

        return tareaGuardada;
    }

    /**
     * Envía notificación por webhook cuando se crea una tarea.
     */
    private void enviarWebhookTareaCreada(Tarea tarea) {
        try {
            // Obtener información del cliente
            Cliente cliente = null;
            if (tarea.getCliente() != null && tarea.getCliente().getIdCliente() != null) {
                cliente = clienteRepository.findById(tarea.getCliente().getIdCliente()).orElse(null);
            }

            TareaWebhookPayload payload = new TareaWebhookPayload(
                    tarea.getCodigo(),
                    cliente != null ? cliente.getNombre() : null,
                    cliente != null ? cliente.getTelefono() : null,
                    cliente != null ? cliente.getCorreo() : null);

            webhookService.enviarNotificacionTareaCreada(payload);
        } catch (Exception e) {
            // No interrumpir el flujo si falla el webhook
            System.err.println("Error preparando webhook: " + e.getMessage());
        }
    }

    /**
     * Envía notificación por webhook cuando se finaliza una tarea.
     * Notifica al asesor creador de la tarea.
     */
    private void enviarWebhookTareaFinalizada(Tarea tarea) {
        try {
            // Obtener información del asesor creador
            Usuario asesor = null;
            if (tarea.getAsesorCrea() != null && tarea.getAsesorCrea().getIdUsuario() != null) {
                asesor = usuarioRepository.findById(tarea.getAsesorCrea().getIdUsuario()).orElse(null);
            }

            // Si no hay asesor o no tiene correo, no enviar notificación
            if (asesor == null || asesor.getCorreo() == null || asesor.getCorreo().isBlank()) {
                System.err
                        .println("No se puede enviar notificación: Asesor sin correo para tarea " + tarea.getIdTarea());
                return;
            }

            // Obtener información del cliente
            Cliente cliente = null;
            if (tarea.getCliente() != null && tarea.getCliente().getIdCliente() != null) {
                cliente = clienteRepository.findById(tarea.getCliente().getIdCliente()).orElse(null);
            }

            // Obtener información del mensajero
            Usuario mensajero = null;
            if (tarea.getMensajeroAsignado() != null && tarea.getMensajeroAsignado().getIdUsuario() != null) {
                mensajero = usuarioRepository.findById(tarea.getMensajeroAsignado().getIdUsuario()).orElse(null);
            }

            // Obtener el nombre del estado
            String estadoNombre = "Finalizada";
            if (tarea.getEstadoTarea() != null && tarea.getEstadoTarea().getNombre() != null) {
                estadoNombre = tarea.getEstadoTarea().getNombre();
            }

            // Formatear fecha de finalización
            String fechaFinStr = tarea.getFechaFin() != null
                    ? tarea.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    : null;

            TareaFinalizadaWebhookPayload payload = new TareaFinalizadaWebhookPayload(
                    tarea.getIdTarea(),
                    tarea.getNombre(),
                    tarea.getCodigo(),
                    estadoNombre,
                    tarea.getObservacion(),
                    fechaFinStr,
                    tarea.getEntregaATiempo(),
                    asesor.getCorreo(),
                    asesor.getNombre() + " " + (asesor.getApellido() != null ? asesor.getApellido() : ""),
                    cliente != null ? cliente.getNombre() : null,
                    mensajero != null
                            ? mensajero.getNombre() + " "
                                    + (mensajero.getApellido() != null ? mensajero.getApellido() : "")
                            : null);

            webhookService.enviarNotificacionTareaFinalizada(payload);
        } catch (Exception e) {
            // No interrumpir el flujo si falla el webhook
            System.err.println("Error preparando webhook de tarea finalizada: " + e.getMessage());
        }
    }

    /**
     * Genera un nuevo código de 4 dígitos para la tarea.
     * Busca el último código existente y le suma 1.
     * Si no hay tareas, empieza desde "0001".
     * El código máximo es "9999".
     */
    private String generarNuevoCodigo() {
        return tareaRepository.findTopByOrderByCodigoDesc()
                .map(ultimaTarea -> {
                    String ultimoCodigo = ultimaTarea.getCodigo();
                    int numero = Integer.parseInt(ultimoCodigo);
                    int nuevoNumero = numero + 1;

                    // Si supera 9999, reiniciar a 0001 (o lanzar excepción según necesidad)
                    if (nuevoNumero > 9999) {
                        nuevoNumero = 1;
                    }

                    return String.format("%04d", nuevoNumero);
                })
                .orElse("0001"); // Si no hay tareas, empezar desde 0001
    }

    @Override
    public Tarea update(Long id, Tarea tarea) {
        Tarea existente = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + id));

        // Actualizamos campos editables
        existente.setNombre(tarea.getNombre());
        existente.setFechaLimite(tarea.getFechaLimite());
        existente.setFechaFin(tarea.getFechaFin());
        existente.setTiempoTotal(tarea.getTiempoTotal());
        existente.setComentario(tarea.getComentario());
        existente.setObservacion(tarea.getObservacion());
        existente.setProceso(tarea.getProceso());
        existente.setFechaInicio(tarea.getFechaInicio());
        existente.setCodigo(tarea.getCodigo());

        // Relaciones
        existente.setTipoOperacion(tarea.getTipoOperacion());
        existente.setCategoria(tarea.getCategoria());
        existente.setCliente(tarea.getCliente());
        existente.setEstadoTarea(tarea.getEstadoTarea());
        existente.setAsesorCrea(tarea.getAsesorCrea());
        existente.setMensajeroAsignado(tarea.getMensajeroAsignado());
        existente.setSupervisorAsigna(tarea.getSupervisorAsigna());

        return tareaRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new IllegalArgumentException("Tarea no encontrada con id: " + id);
        }
        tareaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> findByMensajero(Long idMensajero) {
        return tareaRepository.findByMensajeroAsignadoIdUsuario(idMensajero);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> findTareasActivasByMensajero(Long idMensajero) {
        // Solo traer tareas con estados activos: PENDIENTE, EN PROCESO
        List<String> estadosActivos = List.of("PENDIENTE", "EN PROCESO");
        return tareaRepository.findByMensajeroAsignadoIdUsuarioAndEstadoTareaNombreIn(idMensajero, estadosActivos);
    }

    @Override
    public Tarea asignarMensajero(Long idTarea, Long idMensajero) {
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // Create a reference to the Usuario without loading the full entity
        com.soprint.seguimiento_mensajeros.model.Usuario mensajero = new com.soprint.seguimiento_mensajeros.model.Usuario();
        mensajero.setIdUsuario(idMensajero);

        tarea.setMensajeroAsignado(mensajero);
        return tareaRepository.save(tarea);
    }

    @Override
    public Tarea reasignarMensajero(Long idTarea, Long idMensajero) {
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // Create a reference to the Usuario without loading the full entity
        com.soprint.seguimiento_mensajeros.model.Usuario mensajero = new com.soprint.seguimiento_mensajeros.model.Usuario();
        mensajero.setIdUsuario(idMensajero);

        tarea.setMensajeroAsignado(mensajero);
        return tareaRepository.save(tarea);
    }

    @Override
    public Tarea finalizarTarea(Long idTarea, String codigo, Long idEstadoTarea, String observacion) {
        // 1. Buscar la tarea
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // 2. Validar el código
        if (codigo == null || !codigo.equals(tarea.getCodigo())) {
            throw new IllegalStateException("Código inválido");
        }

        // 3. Establecer fecha de finalización
        LocalDateTime fechaFin = LocalDateTime.now();
        tarea.setFechaFin(fechaFin);

        // 4. Calcular tiempoTotal (desde fechaCreacion hasta fechaFin) en minutos
        if (tarea.getFechaCreacion() != null) {
            long minutosTotal = java.time.Duration.between(tarea.getFechaCreacion(), fechaFin).toMinutes();
            tarea.setTiempoTotal(minutosTotal);
        }

        // 5. Calcular tiempoEjecucion (desde fechaInicio hasta fechaFin) en minutos
        if (tarea.getFechaInicio() != null) {
            long minutosEjecucion = java.time.Duration.between(tarea.getFechaInicio(), fechaFin).toMinutes();
            tarea.setTiempoEjecucion(minutosEjecucion);
        }

        // 6. Actualizar estado de la tarea
        if (idEstadoTarea != null) {
            com.soprint.seguimiento_mensajeros.model.EstadoTarea nuevoEstado = new com.soprint.seguimiento_mensajeros.model.EstadoTarea();
            nuevoEstado.setIdEstadoTarea(idEstadoTarea);
            tarea.setEstadoTarea(nuevoEstado);
        }

        // 7. Guardar observación
        if (observacion != null) {
            tarea.setObservacion(observacion);
        }

        // 8. Calcular si la entrega fue a tiempo
        if (tarea.getFechaLimite() != null) {
            tarea.setEntregaATiempo(!fechaFin.isAfter(tarea.getFechaLimite()));
        }

        // 9. Guardar la tarea
        Tarea tareaFinalizada = tareaRepository.save(tarea);

        // 10. Enviar notificación al asesor creador
        enviarWebhookTareaFinalizada(tareaFinalizada);

        return tareaFinalizada;
    }

    @Override
    public Tarea finalizarTareaSinCodigo(Long idTarea, Long idEstadoTarea, String observacion) {
        // 1. Buscar la tarea
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // 2. Establecer fecha de finalización
        LocalDateTime fechaFin = LocalDateTime.now();
        tarea.setFechaFin(fechaFin);

        // 3. Calcular tiempoTotal (desde fechaCreacion hasta fechaFin) en minutos
        if (tarea.getFechaCreacion() != null) {
            long minutosTotal = java.time.Duration.between(tarea.getFechaCreacion(), fechaFin).toMinutes();
            tarea.setTiempoTotal(minutosTotal);
        }

        // 4. Calcular tiempoEjecucion (desde fechaInicio hasta fechaFin) en minutos
        if (tarea.getFechaInicio() != null) {
            long minutosEjecucion = java.time.Duration.between(tarea.getFechaInicio(), fechaFin).toMinutes();
            tarea.setTiempoEjecucion(minutosEjecucion);
        }

        // 5. Actualizar estado de la tarea
        if (idEstadoTarea != null) {
            com.soprint.seguimiento_mensajeros.model.EstadoTarea nuevoEstado = new com.soprint.seguimiento_mensajeros.model.EstadoTarea();
            nuevoEstado.setIdEstadoTarea(idEstadoTarea);
            tarea.setEstadoTarea(nuevoEstado);
        }

        // 6. Guardar observación
        if (observacion != null) {
            tarea.setObservacion(observacion);
        }

        // 7. Calcular si la entrega fue a tiempo
        if (tarea.getFechaLimite() != null) {
            tarea.setEntregaATiempo(!fechaFin.isAfter(tarea.getFechaLimite()));
        }

        // 8. Guardar la tarea
        Tarea tareaFinalizada = tareaRepository.save(tarea);

        // 9. Enviar notificación al asesor creador
        enviarWebhookTareaFinalizada(tareaFinalizada);

        return tareaFinalizada;
    }

    @Override
    public void reenviarCodigoTarea(Long idTarea) {
        // 1. Buscar la tarea
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // 2. Obtener información del cliente
        Cliente cliente = null;
        if (tarea.getCliente() != null && tarea.getCliente().getIdCliente() != null) {
            cliente = clienteRepository.findById(tarea.getCliente().getIdCliente()).orElse(null);
        }

        // 3. Crear el payload
        TareaWebhookPayload payload = new TareaWebhookPayload(
                tarea.getCodigo(),
                cliente != null ? cliente.getNombre() : null,
                cliente != null ? cliente.getTelefono() : null,
                cliente != null ? cliente.getCorreo() : null);

        // 4. Enviar al webhook
        webhookService.enviarNotificacionTareaCreada(payload);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> findByRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return tareaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> findTareasCompletadasByMensajeroAndFechas(Long idMensajero, LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        return tareaRepository.findByMensajeroAsignadoIdUsuarioAndEstadoTareaNombreAndFechaFinBetween(
                idMensajero, "COMPLETADA", fechaInicio, fechaFin);
    }

    @Override
    public Tarea iniciarTarea(Long idTarea) {
        // 1. Buscar la tarea
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con id: " + idTarea));

        // 2. Cambiar estado a EN PROCESO (id = 2)
        com.soprint.seguimiento_mensajeros.model.EstadoTarea estadoEnProceso = new com.soprint.seguimiento_mensajeros.model.EstadoTarea();
        estadoEnProceso.setIdEstadoTarea(2L);
        tarea.setEstadoTarea(estadoEnProceso);

        // 3. Registrar fecha de inicio
        LocalDateTime fechaInicio = LocalDateTime.now();
        tarea.setFechaInicio(fechaInicio);

        // 4. Calcular tiempo de respuesta (desde asignación/creación hasta inicio)
        if (tarea.getFechaCreacion() != null) {
            long minutosRespuesta = java.time.Duration.between(tarea.getFechaCreacion(), fechaInicio).toMinutes();
            tarea.setTiempoRespuesta(minutosRespuesta);
        }

        // 5. Guardar y retornar
        return tareaRepository.save(tarea);
    }
}
