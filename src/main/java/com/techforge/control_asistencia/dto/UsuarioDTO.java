package com.techforge.control_asistencia.dto;

import com.techforge.control_asistencia.model.Usuario;

public class UsuarioDTO {
    private Long id;
    private String usuario;
    private String email;

    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String usuario, String email) {
        this.id = id;
        this.usuario = usuario;
        this.email = email;
    }

    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsuario(), u.getEmail());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
