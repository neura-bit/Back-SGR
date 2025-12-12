package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TareaService implements ITareaService {

    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
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

        return tareaRepository.save(tarea);
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
}
