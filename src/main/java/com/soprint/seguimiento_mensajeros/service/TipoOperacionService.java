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
    /**
     * Devuelve TODOS los tipos, activos e inactivos, a diferencia de
     * SucursalService, que filtra por activo.
     *
     * Es deliberado: la web resuelve el nombre del tipo de cada tarea contra
     * esta lista. Si se ocultaran los inactivos, las tareas historicas hechas
     * con un tipo dado de baja se quedarian sin nombre. Quien filtra es el
     * formulario de creacion, que solo ofrece los activos.
     */
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
        tipoOperacion.setActivo(true);
        return tipoOperacionRepository.save(tipoOperacion);
    }

    @Override
    public TipoOperacion update(Long id, TipoOperacion tipoOperacion) {
        TipoOperacion existente = tipoOperacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TipoOperacion no encontrado con id: " + id));

        existente.setNombre(tipoOperacion.getNombre());
        // Solo se toca si viene en la peticion: asi una edicion de nombre no
        // reactiva sin querer un tipo dado de baja. Es tambien la via para
        // volver a activarlo.
        if (tipoOperacion.getActivo() != null) {
            existente.setActivo(tipoOperacion.getActivo());
        }
        if (existente.getActivo() == null) {
            existente.setActivo(true);   // filas anteriores a esta columna
        }

        return tipoOperacionRepository.save(existente);
    }

    /**
     * Desactiva en lugar de borrar. Un tipo de operacion referenciado por
     * tareas no se puede eliminar sin romper la clave foranea, y el historial
     * debe seguir mostrando con que tipo se hizo cada tarea.
     */
    @Override
    public void delete(Long id) {
        TipoOperacion tipoOperacion = tipoOperacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TipoOperacion no encontrado con id: " + id));
        tipoOperacion.setActivo(false);
        tipoOperacionRepository.save(tipoOperacion);
    }
}
