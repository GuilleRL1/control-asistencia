package com.techforge.control_asistencia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String password; // guardar encriptado (BCrypt)

    @Column(unique = true)
    private String email; // usado para recuperar la contraseña

    @Column(name = "reset_token")
    private String resetToken; // token de recuperación (en producción guardar su hash)

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry; // expiración del token de recuperación

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getResetToken() { return resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }

    public void setId(Long id) { this.id = id; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
}
