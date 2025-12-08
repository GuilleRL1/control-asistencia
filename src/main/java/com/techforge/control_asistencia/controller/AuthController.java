package com.techforge.control_asistencia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Usuario;
import com.techforge.control_asistencia.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Login: valida usuario y contraseña
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario login) {
        Usuario u = usuarioRepo.findByUsuario(login.getUsuario());
        if (u == null) return ResponseEntity.status(401).body("Usuario no encontrado");
        if (!passwordEncoder.matches(login.getPassword(), u.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }
        return ResponseEntity.ok("OK"); // en producción retornarías un token/sesión
    }

    // ✅ Registrar nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario nuevo) {
        if (nuevo.getUsuario() == null || nuevo.getUsuario().isBlank() ||
            nuevo.getPassword() == null || nuevo.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Usuario y contraseña son obligatorios");
        }
        if (usuarioRepo.findByUsuario(nuevo.getUsuario()) != null) {
            return ResponseEntity.badRequest().body("Ya existe un usuario con ese nombre");
        }
        nuevo.setPassword(passwordEncoder.encode(nuevo.getPassword())); // encriptar
        usuarioRepo.save(nuevo);
        return ResponseEntity.ok("Usuario creado");
    }

    // ✅ DTO interno para cambio de contraseña
    public static class PasswordDTO {
        public String actual;
        public String nueva;
    }

    // ✅ Cambiar contraseña
    @PutMapping("/password/{usuario}")
    public ResponseEntity<?> cambiarPassword(@PathVariable String usuario, @RequestBody PasswordDTO dto) {
        if (dto.nueva == null || dto.nueva.isBlank()) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía");
        }

        Usuario u = usuarioRepo.findByUsuario(usuario);
        if (u == null) return ResponseEntity.status(404).body("Usuario no encontrado");

        if (!passwordEncoder.matches(dto.actual, u.getPassword())) {
            return ResponseEntity.badRequest().body("Contraseña actual incorrecta");
        }

        u.setPassword(passwordEncoder.encode(dto.nueva));
        usuarioRepo.save(u);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}