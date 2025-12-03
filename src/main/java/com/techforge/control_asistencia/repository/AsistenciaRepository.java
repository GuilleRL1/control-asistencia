package com.techforge.control_asistencia.repository;

import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByEmpleado(Empleado empleado);
}

