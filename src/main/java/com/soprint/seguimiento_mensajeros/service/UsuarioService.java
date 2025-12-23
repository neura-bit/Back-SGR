package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario create(Usuario usuario) {

        if (usuarioRepository.existsByUsername(usuario.username)) {
            throw new IllegalArgumentException("El username ya está registrado.");
        }

        usuario.idUsuario = null; // que se genere
        usuario.fechaCreacion = LocalDateTime.now();
        usuario.estado = true; // activo por defecto

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(Long id, Usuario usuario) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        existente.nombre = usuario.nombre;
        existente.apellido = usuario.apellido;
        existente.telefono = usuario.telefono;
        existente.username = usuario.username;
        existente.correo = usuario.correo;
        existente.estado = usuario.estado;
        existente.sucursal = usuario.sucursal;
        existente.rol = usuario.rol;

        // Actualizar password si se proporciona una nueva (no vacía)
        if (usuario.password != null && !usuario.password.trim().isEmpty()) {
            existente.password = passwordEncoder.encode(usuario.password);
        }

        return usuarioRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
