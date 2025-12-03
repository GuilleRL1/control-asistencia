package com.techforge.control_asistencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techforge.control_asistencia.model.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    Turno findByEmpleadoId(Long empleadoId);
}