package com.techforge.control_asistencia.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    // Tiempo de validez del token de recuperación (minutos)
    private static final int TOKEN_VALIDITY_MINUTES = 30;

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

        // Normalizar y validar email opcional
        if (nuevo.getEmail() != null && !nuevo.getEmail().isBlank()) {
            String email = nuevo.getEmail().trim().toLowerCase();
            if (usuarioRepo.findByEmail(email) != null) {
                return ResponseEntity.badRequest().body("Ya existe un usuario con ese email");
            }
            nuevo.setEmail(email);
        }

        nuevo.setPassword(passwordEncoder.encode(nuevo.getPassword())); // encriptar
        usuarioRepo.save(nuevo);
        return ResponseEntity.ok("Usuario creado");
    }

    // ✅ DTO interno para solicitar recuperación de contraseña
    public static class ForgotPasswordDTO {
        public String email;
    }

    // ✅ Solicitar recuperación de contraseña (genera y "envía" el enlace)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        String email = dto == null ? null : dto.email;
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("El email es obligatorio");
        }

        Usuario u = usuarioRepo.findByEmail(email.trim().toLowerCase());

        // Mensaje genérico para no revelar si el email existe o no
        if (u == null) {
            return ResponseEntity.ok(Map.of("message",
                    "Si el email está registrado, recibirás un enlace de recuperación."));
        }

        // Generar token de recuperación y su expiración
        String token = UUID.randomUUID().toString();
        u.setResetToken(token);
        u.setResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES));
        usuarioRepo.save(u);

        String resetLink = "http://localhost:8080/RegistroAsistencia/index.html#recuperar?token=" + token;

        // 📧 Simulación de envío de email (no hay SMTP configurado en el proyecto)
        System.out.println("===== SIMULACIÓN DE EMAIL DE RECUPERACIÓN =====");
        System.out.println("Para: " + email);
        System.out.println("Asunto: Recuperación de contraseña");
        System.out.println("Enlace: " + resetLink);
        System.out.println("===============================================");

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Enlace de recuperación generado (email simulado).");
        resp.put("resetToken", token);
        resp.put("resetLink", resetLink);
        return ResponseEntity.ok(resp);
    }

    // ✅ DTO interno para restablecer la contraseña con token
    public static class ResetPasswordDTO {
        public String token;
        public String nueva;
    }

    // ✅ Restablecer contraseña usando el token de recuperación
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        if (dto.token == null || dto.token.isBlank()) {
            return ResponseEntity.badRequest().body("El token es obligatorio");
        }
        if (dto.nueva == null || dto.nueva.isBlank()) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía");
        }

        Usuario u = usuarioRepo.findByResetToken(dto.token.trim());
        if (u == null || u.getResetTokenExpiry() == null
                || u.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Token inválido o expirado");
        }

        u.setPassword(passwordEncoder.encode(dto.nueva));
        u.setResetToken(null);
        u.setResetTokenExpiry(null);
        usuarioRepo.save(u);

        return ResponseEntity.ok("Contraseña restablecida correctamente");
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
