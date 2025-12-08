package com.techforge.control_asistencia.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Alerta;
import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.repository.AlertaRepository;
import com.techforge.control_asistencia.repository.EmpleadoRepository;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    // ✅ Obtener todas las alertas (ordenadas por fecha descendente)
    @GetMapping
    public List<Alerta> getAllAlertas() {
        return alertaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }

    // ✅ Crear una alerta manualmente
    @PostMapping
    public ResponseEntity<?> crearAlerta(@RequestBody Alerta alerta) {
        if (alerta.getEmpleadoId() == null || alerta.getNombreEmpleado() == null || alerta.getDetalle() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debes indicar empleadoId, nombreEmpleado y detalle"));
        }
        alerta.setFecha(LocalDateTime.now());
        return ResponseEntity.ok(alertaRepository.save(alerta));
    }

    // ✅ Obtener alertas por ID de empleado
    @GetMapping("/empleado/{id}")
    public ResponseEntity<?> getAlertasPorEmpleado(@PathVariable Long id) {
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(id);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Empleado no encontrado"));
        }
        List<Alerta> alertas = alertaRepository.findByEmpleadoId(id);
        return ResponseEntity.ok(alertas);
    }
}
