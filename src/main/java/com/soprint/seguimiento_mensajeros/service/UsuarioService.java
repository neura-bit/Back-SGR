package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Rol;
import com.soprint.seguimiento_mensajeros.model.Sucursal;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.RolRepository;
import com.soprint.seguimiento_mensajeros.repository.SucursalRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            SucursalRepository sucursalRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
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

        // Buscar Sucursal y Rol de la base de datos para evitar
        // TransientPropertyValueException
        if (usuario.sucursal != null && usuario.sucursal.getIdSucursal() != null) {
            Sucursal sucursal = sucursalRepository.findById(usuario.sucursal.getIdSucursal())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sucursal no encontrada con id: " + usuario.sucursal.getIdSucursal()));
            usuario.sucursal = sucursal;
        }

        if (usuario.rol != null && usuario.rol.getId_rol() != null) {
            Rol rol = rolRepository.findById(usuario.rol.getId_rol())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Rol no encontrado con id: " + usuario.rol.getId_rol()));
            usuario.rol = rol;
        }

        // Encriptar password si se proporciona
        if (usuario.password != null && !usuario.password.trim().isEmpty()) {
            usuario.password = passwordEncoder.encode(usuario.password);
        }

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

        // Buscar Sucursal y Rol de la base de datos para evitar
        // TransientPropertyValueException
        if (usuario.sucursal != null && usuario.sucursal.getIdSucursal() != null) {
            Sucursal sucursal = sucursalRepository.findById(usuario.sucursal.getIdSucursal())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sucursal no encontrada con id: " + usuario.sucursal.getIdSucursal()));
            existente.sucursal = sucursal;
        }

        if (usuario.rol != null && usuario.rol.getId_rol() != null) {
            Rol rol = rolRepository.findById(usuario.rol.getId_rol())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Rol no encontrado con id: " + usuario.rol.getId_rol()));
            existente.rol = rol;
        }

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
