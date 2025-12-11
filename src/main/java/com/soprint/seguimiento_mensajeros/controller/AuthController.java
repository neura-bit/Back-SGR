package com.soprint.seguimiento_mensajeros.controller;

import com.soprint.seguimiento_mensajeros.DTO.LoginRequest;
import com.soprint.seguimiento_mensajeros.DTO.LoginResponse;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import com.soprint.seguimiento_mensajeros.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Find user by username
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        // Check if user is active
        if (usuario.getEstado() == null || !usuario.getEstado()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario inactivo"));
        }

        // Validate password (support both plain text and BCrypt for migration)
        boolean passwordValid = false;
        String storedPassword = usuario.getPassword();

        // Check if password is already BCrypt encoded (starts with $2a$, $2b$, or $2y$)
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            // Password is BCrypt encoded
            passwordValid = passwordEncoder.matches(request.getPassword(), storedPassword);
        } else {
            // Password is plain text - validate and migrate
            if (storedPassword != null && storedPassword.equals(request.getPassword())) {
                passwordValid = true;
                // Migrate password to BCrypt
                usuario.setPassword(passwordEncoder.encode(request.getPassword()));
                usuarioRepository.save(usuario);
            }
        }

        if (!passwordValid) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }

        // Generate JWT token
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + (usuario.getRol() != null ? usuario.getRol().getNombre() : "USER"))));

        String token = jwtService.generateToken(userDetails);

        // Build response
        LoginResponse response = new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : "USER",
                usuario.getIdUsuario(),
                jwtService.getExpirationTime());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no proporcionado"));
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
            }

            return ResponseEntity.ok(Map.of(
                    "idUsuario", usuario.getIdUsuario(),
                    "username", usuario.getUsername(),
                    "nombre", usuario.getNombre(),
                    "apellido", usuario.getApellido(),
                    "rol", usuario.getRol() != null ? usuario.getRol().getNombre() : "USER",
                    "sucursal", usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
    }
}
