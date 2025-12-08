package com.techforge.control_asistencia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.dto.ReporteAsistenciaDTO;
import com.techforge.control_asistencia.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // ✅ Endpoint para reporte de asistencias
    @GetMapping("/asistencias")
    public ResponseEntity<List<ReporteAsistenciaDTO>> reporteAsistencias(
            @RequestParam String inicio,
            @RequestParam String fin) {
        return ResponseEntity.ok(reporteService.generarAsistencias(inicio, fin));
    }
}