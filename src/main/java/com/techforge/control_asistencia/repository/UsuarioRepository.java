package com.techforge.control_asistencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techforge.control_asistencia.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsuario(String usuario);
}
