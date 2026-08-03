package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    List<Cliente> findAll();

    Optional<Cliente> findById(Long id);

    /** @param autor usuario autenticado que registra el cliente; puede ser null. */
    Cliente create(Cliente cliente, Usuario autor);

    /** @param autor usuario autenticado que modifica el cliente; puede ser null. */
    Cliente update(Long id, Cliente cliente, Usuario autor);

    void delete(Long id);
}
