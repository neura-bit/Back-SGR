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
        tarea.setIdTarea(null);                  // que el ID lo genere la BD
        tarea.setFechaCreacion(LocalDateTime.now());  // fecha de creación automática

        // aquí podrías poner un estado inicial por defecto si quieres
        // tarea.setEstadoTarea(estadoInicial);

        return tareaRepository.save(tarea);
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
}
