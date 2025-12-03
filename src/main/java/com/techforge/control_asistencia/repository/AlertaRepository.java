package com.techforge.control_asistencia.repository;

import com.techforge.control_asistencia.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;



public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Buscar alertas por empleado
    List<Alerta> findByEmpleadoId(Long empleadoId);
}