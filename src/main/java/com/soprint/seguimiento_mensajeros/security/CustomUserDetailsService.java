package com.soprint.seguimiento_mensajeros.security;

import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Permiso extra que se suma al rol de un supervisor habilitado para Rendimiento. */
    public static final String PERMISO_VER_RENDIMIENTO = "VER_RENDIMIENTO";

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Check if user is active
        if (usuario.getEstado() == null || !usuario.getEstado()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Get role name from the related Rol entity
        String roleName = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        // Se lee de la BD en cada request: dar o quitar el permiso aplica al instante en la API.
        if (usuario.puedeVerRendimiento()) {
            authorities.add(new SimpleGrantedAuthority(PERMISO_VER_RENDIMIENTO));
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                authorities);
    }

    /**
     * Get the Usuario entity by username (used for password migration)
     */
    public Usuario getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
