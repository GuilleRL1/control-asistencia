package com.techforge.control_asistencia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techforge.control_asistencia.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Buscar alertas por empleadoId
    List<Alerta> findByEmpleadoId(Long empleadoId);

    // Buscar alertas en un rango de fecha/hora
    List<Alerta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
