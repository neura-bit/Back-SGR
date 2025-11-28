package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoOperacion;

import java.util.List;
import java.util.Optional;

public interface ITipoOperacionService {

    List<TipoOperacion> findAll();

    Optional<TipoOperacion> findById(Long id);

    TipoOperacion create(TipoOperacion tipoOperacion);

    TipoOperacion update(Long id, TipoOperacion tipoOperacion);

    void delete(Long id);
}
