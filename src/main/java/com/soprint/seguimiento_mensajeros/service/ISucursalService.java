package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Sucursal;

import java.util.List;
import java.util.Optional;

public interface ISucursalService {

    List<Sucursal> findAll();

    Optional<Sucursal> findById(Long id);

    Sucursal create(Sucursal sucursal);

    Sucursal update(Long id, Sucursal sucursal);

    void delete(Long id);
}
