package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSimilitud.Coincidencia> buscarSimilares(Cliente candidato, int maximo) {
        if (candidato == null) {
            return List.of();
        }
        // Se recorre la tabla completa en memoria: son ~1300 clientes y las
        // reglas (tildes, cédula vs RUC, distancia en metros) no se expresan
        // bien en SQL. Si la tabla creciera un orden de magnitud habría que
        // prefiltrar por ciudad o por un bloque del nombre antes de puntuar.
        return clienteRepository.findAll().stream()
                // Al editar, el propio cliente no debe contarse como parecido
                .filter(existente -> !Objects.equals(existente.getIdCliente(), candidato.getIdCliente()))
                .map(existente -> ClienteSimilitud.comparar(candidato, existente))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ClienteSimilitud.Coincidencia::getPuntaje).reversed())
                .limit(maximo)
                .collect(Collectors.toList());
    }

    @Override
    public Cliente create(Cliente cliente, Usuario autor) {
        cliente.setIdCliente(null); // que se genere el ID

        // La autoría se toma siempre del usuario autenticado, nunca del cuerpo
        // de la petición, para que no pueda falsearse.
        cliente.setCreadoPor(autor);
        cliente.setFechaCreacion(LocalDateTime.now());
        cliente.setModificadoPor(null);
        cliente.setFechaModificacion(null);

        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Long id, Cliente cliente, Usuario autor) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));

        // Actualiza solo los campos editables
        existente.setNombre(cliente.getNombre());
        existente.setTelefono(cliente.getTelefono());
        existente.setRucCi(cliente.getRucCi());
        existente.setDireccion(cliente.getDireccion());
        existente.setCiudad(cliente.getCiudad());
        existente.setLatitud(cliente.getLatitud());
        existente.setLongitud(cliente.getLongitud());
        existente.setDetalle(cliente.getDetalle());
        existente.setCorreo(cliente.getCorreo());

        // El creador original se conserva; solo se registra la última modificación
        existente.setModificadoPor(autor);
        existente.setFechaModificacion(LocalDateTime.now());

        return clienteRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }
}
