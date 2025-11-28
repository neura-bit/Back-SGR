package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.EstadoTarea;
import com.soprint.seguimiento_mensajeros.repository.EstadoTareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EstadoTareaService implements IEstadoTareaService {

    private final EstadoTareaRepository estadoTareaRepository;

    public EstadoTareaService(EstadoTareaRepository estadoTareaRepository) {
        this.estadoTareaRepository = estadoTareaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoTarea> findAll() {
        return estadoTareaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EstadoTarea> findById(Long id) {
        return estadoTareaRepository.findById(id);
    }

    @Override
    public EstadoTarea create(EstadoTarea estadoTarea) {
        estadoTarea.setIdEstadoTarea(null);
        return estadoTareaRepository.save(estadoTarea);
    }

    @Override
    public EstadoTarea update(Long id, EstadoTarea estadoTarea) {
        EstadoTarea existente = estadoTareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EstadoTarea no encontrado con id: " + id));

        existente.setNombre(estadoTarea.getNombre());

        return estadoTareaRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!estadoTareaRepository.existsById(id)) {
            throw new IllegalArgumentException("EstadoTarea no encontrado con id: " + id);
        }
        estadoTareaRepository.deleteById(id);
    }
}
