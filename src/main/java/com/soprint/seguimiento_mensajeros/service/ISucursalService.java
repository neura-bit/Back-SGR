package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Sucursal;

import java.util.List;
import java.util.Optional;

public interface ISucursalService {

    List<Sucursal> findAll();

    Optional<Sucursal> findById(Long id);

    Sucursal create(Sucursal sucursal);

    Sucursal update(Long id, Sucursal sucursal);

    /**
     * Ciudades donde la empresa tiene sucursal, sin repetir y ya normalizadas.
     * Alimenta el desplegable de ciudad del formulario de cliente, para que
     * `cliente.ciudad` deje de ser texto libre y las tareas se puedan agrupar
     * por ciudad de forma fiable.
     */
    List<String> findCiudades();

    void delete(Long id);
}
