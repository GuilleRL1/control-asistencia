package com.techforge.control_asistencia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techforge.control_asistencia.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Buscar alertas por empleadoId
    List<Alerta> findByEmpleadoId(Long empleadoId);
}