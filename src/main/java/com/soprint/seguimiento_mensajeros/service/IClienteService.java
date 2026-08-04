package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    List<Cliente> findAll();

    Optional<Cliente> findById(Long id);

    /**
     * Clientes ya registrados que se parecen al que se quiere crear, ordenados
     * del más parecido al menos.
     *
     * No impide crear nada: sirve para avisar al asesor y darle la opción de
     * revisar el cliente existente antes de duplicarlo.
     *
     * @param maximo tope de resultados, para no abrumar en pantalla
     */
    List<ClienteSimilitud.Coincidencia> buscarSimilares(Cliente candidato, int maximo);

    /** @param autor usuario autenticado que registra el cliente; puede ser null. */
    Cliente create(Cliente cliente, Usuario autor);

    /** @param autor usuario autenticado que modifica el cliente; puede ser null. */
    Cliente update(Long id, Cliente cliente, Usuario autor);

    void delete(Long id);
}
