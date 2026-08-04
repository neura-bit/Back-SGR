package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.ClienteResponse;
import com.soprint.seguimiento_mensajeros.DTO.ClienteSimilarResponse;
import com.soprint.seguimiento_mensajeros.model.Cliente;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.service.ClienteSimilitud;
import com.soprint.seguimiento_mensajeros.service.IClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final IClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;

    public ClienteController(IClienteService clienteService,
                             UsuarioRepository usuarioRepository,
                             TareaRepository tareaRepository) {
        this.clienteService = clienteService;
        this.usuarioRepository = usuarioRepository;
        this.tareaRepository = tareaRepository;
    }

    /** Resuelve el usuario autenticado; null si no se puede determinar. */
    private Usuario usuarioActual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return usuarioRepository.findByUsername(authentication.getName()).orElse(null);
    }

    /**
     * Los datos de auditoría (quién creó y quién modificó) son visibles
     * únicamente para administradores.
     */
    private boolean puedeVerAuditoria(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    // GET /api/clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(Authentication authentication) {
        boolean conAuditoria = puedeVerAuditoria(authentication);
        List<ClienteResponse> clientes = clienteService.findAll().stream()
                .map(cliente -> ClienteResponse.fromEntity(cliente, conAuditoria))
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // GET /api/clientes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long id,
                                                        Authentication authentication) {
        boolean conAuditoria = puedeVerAuditoria(authentication);
        return clienteService.findById(id)
                .map(cliente -> ClienteResponse.fromEntity(cliente, conAuditoria))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/clientes/similares
     *
     * Devuelve los clientes ya registrados que se parecen al que se está por
     * crear, para poder avisar "este cliente ya existe" antes de duplicarlo.
     *
     * Es una consulta, no una creación: no persiste nada. Se usa POST y no GET
     * porque recibe el mismo cuerpo que la creación (nombre, RUC, dirección,
     * coordenadas), que como query string quedaría poco manejable.
     *
     * No se valida con @Valid a propósito: se consulta con el formulario a
     * medio llenar, así que los campos obligatorios pueden faltar todavía.
     */
    @PostMapping("/similares")
    public ResponseEntity<List<ClienteSimilarResponse>> buscarSimilares(@RequestBody Cliente candidato) {
        List<ClienteSimilitud.Coincidencia> coincidencias = clienteService.buscarSimilares(candidato, 5);
        if (coincidencias.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> ids = coincidencias.stream()
                .map(c -> c.getCliente().getIdCliente())
                .collect(Collectors.toList());
        Map<Long, Long> tareasPorCliente = tareaRepository.contarPorClientes(ids).stream()
                .collect(Collectors.toMap(fila -> (Long) fila[0], fila -> (Long) fila[1]));

        List<ClienteSimilarResponse> respuesta = coincidencias.stream()
                .map(c -> ClienteSimilarResponse.from(
                        c, tareasPorCliente.getOrDefault(c.getCliente().getIdCliente(), 0L)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    // POST /api/clientes
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody Cliente cliente,
                                                 Authentication authentication) {
        Cliente creado = clienteService.create(cliente, usuarioActual(authentication));
        return ResponseEntity
                .created(URI.create("/api/clientes/" + creado.getIdCliente()))
                .body(ClienteResponse.fromEntity(creado, puedeVerAuditoria(authentication)));
    }

    // PUT /api/clientes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody Cliente cliente,
                                                      Authentication authentication) {
        try {
            Cliente actualizado = clienteService.update(id, cliente, usuarioActual(authentication));
            return ResponseEntity.ok(ClienteResponse.fromEntity(actualizado, puedeVerAuditoria(authentication)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
