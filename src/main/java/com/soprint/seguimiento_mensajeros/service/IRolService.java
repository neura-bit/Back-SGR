package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Rol;

import java.util.List;
import java.util.Optional;

public interface IRolService {

    List<Rol> findAll();

    Optional<Rol> findById(Long id);

    Rol create(Rol rol);

    Rol update(Long id, Rol rol);

    void delete(Long id);
}
