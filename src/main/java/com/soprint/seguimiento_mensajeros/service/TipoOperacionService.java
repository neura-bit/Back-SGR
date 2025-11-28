package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoOperacion;
import com.soprint.seguimiento_mensajeros.repository.TipoOperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TipoOperacionService implements ITipoOperacionService {

    private final TipoOperacionRepository tipoOperacionRepository;

    public TipoOperacionService(TipoOperacionRepository tipoOperacionRepository) {
        this.tipoOperacionRepository = tipoOperacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoOperacion> findAll() {
        return tipoOperacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoOperacion> findById(Long id) {
        return tipoOperacionRepository.findById(id);
    }

    @Override
    public TipoOperacion create(TipoOperacion tipoOperacion) {
        tipoOperacion.setIdTipoOperacion(null); // generar ID
        return tipoOperacionRepository.save(tipoOperacion);
    }

    @Override
    public TipoOperacion update(Long id, TipoOperacion tipoOperacion) {
        TipoOperacion existente = tipoOperacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TipoOperacion no encontrado con id: " + id));

        existente.setNombre(tipoOperacion.getNombre());

        return tipoOperacionRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!tipoOperacionRepository.existsById(id)) {
            throw new IllegalArgumentException("TipoOperacion no encontrado con id: " + id);
        }
        tipoOperacionRepository.deleteById(id);
    }
}
