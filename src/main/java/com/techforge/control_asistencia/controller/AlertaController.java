package com.techforge.control_asistencia.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Alerta;
import com.techforge.control_asistencia.repository.AlertaRepository;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    // Obtener todas las alertas
    @GetMapping
    public List<Alerta> getAllAlertas() {
        return alertaRepository.findAll();
    }

    // Crear una alerta
    @PostMapping
    public Alerta crearAlerta(@RequestBody Alerta alerta) {
        alerta.setFecha(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

    // Obtener alertas por empleado
    @GetMapping("/empleado/{id}")
    public List<Alerta> getAlertasPorEmpleado(@PathVariable Long id) {
        return alertaRepository.findByEmpleadoId(id);
    }
}
