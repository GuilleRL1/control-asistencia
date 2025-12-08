package com.techforge.control_asistencia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Empleado;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByEmpleado(Empleado empleado);

    // ✅ Buscar asistencias de un empleado en un rango de fecha/hora
    List<Asistencia> findByEmpleadoAndFechaHoraBetween(
            Empleado empleado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ✅ Nuevo: buscar todas las asistencias en un rango de fecha/hora
    List<Asistencia> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}
