package com.techforge.control_asistencia.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.dto.UsuarioDTO;
import com.techforge.control_asistencia.model.Usuario;
import com.techforge.control_asistencia.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // DTO de entrada para crear/actualizar usuarios
    public static class UsuarioRequest {
        public String usuario;
        public String password;
        public String email;
    }

    // Listar usuarios (sin exponer contraseñas)
    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioRepo.findAll().stream().map(UsuarioDTO::from).collect(Collectors.toList());
    }

    // Crear usuario
    @PostMapping
    public ResponseEntity<Object> crear(@RequestBody UsuarioRequest req) {
        if (req == null || isBlank(req.usuario) || isBlank(req.password)) {
            return ResponseEntity.badRequest().body("Usuario y contraseña son obligatorios");
        }
        if (usuarioRepo.findByUsuario(req.usuario.trim()) != null) {
            return ResponseEntity.badRequest().body("Ya existe un usuario con ese nombre");
        }

        String email = normalizeEmail(req.email);
        if (email != null && usuarioRepo.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Ya existe un usuario con ese email");
        }

        Usuario u = new Usuario();
        u.setUsuario(req.usuario.trim());
        u.setPassword(passwordEncoder.encode(req.password));
        u.setEmail(email);
        usuarioRepo.save(u);
        return ResponseEntity.ok(UsuarioDTO.from(u));
    }

    // Actualizar usuario (nombre/email y, opcionalmente, contraseña)
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @RequestBody UsuarioRequest req) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        if (req != null && !isBlank(req.usuario) && !req.usuario.trim().equals(u.getUsuario())) {
            if (usuarioRepo.findByUsuario(req.usuario.trim()) != null) {
                return ResponseEntity.badRequest().body("Ya existe un usuario con ese nombre");
            }
            u.setUsuario(req.usuario.trim());
        }

        String email = normalizeEmail(req == null ? null : req.email);
        if (email != null && !email.equals(u.getEmail()) && usuarioRepo.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Ya existe un usuario con ese email");
        }
        if (req != null && email != null) {
            u.setEmail(email);
        }

        if (req != null && !isBlank(req.password)) {
            u.setPassword(passwordEncoder.encode(req.password));
        }

        usuarioRepo.save(u);
        return ResponseEntity.ok(UsuarioDTO.from(u));
    }

    // Eliminar usuario (se protege al administrador por defecto)
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        if ("admin".equals(u.getUsuario())) {
            return ResponseEntity.badRequest().body("No se puede eliminar el usuario administrador por defecto");
        }
        usuarioRepo.delete(u);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
