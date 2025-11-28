package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICategoriaService {

    List<Categoria> findAll();

    Optional<Categoria> findById(Long id);

    Categoria create(Categoria categoria);

    Categoria update(Long id, Categoria categoria);

    void delete(Long id);
}
