package com.techforge.control_asistencia.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.repository.AsistenciaRepository;
import com.techforge.control_asistencia.repository.EmpleadoRepository;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    @Autowired
    private AsistenciaRepository asistenciaRepo;

    @Autowired
    private EmpleadoRepository empleadoRepo;

    // ✅ Registrar asistencia por cédula y tipo (entrada/salida)
    @PostMapping("/{cedula}/{tipo}")
    public ResponseEntity<?> registrar(@PathVariable String cedula, @PathVariable String tipo) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findByCedula(cedula);

        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }

        if (!("entrada".equalsIgnoreCase(tipo) || "salida".equalsIgnoreCase(tipo))) {
            return ResponseEntity.badRequest().body("tipo debe ser 'entrada' o 'salida'");
        }

        Empleado empleado = empleadoOpt.get();
        Asistencia a = new Asistencia(empleado, null, tipo.toLowerCase());
        Asistencia saved = asistenciaRepo.save(a);

        return ResponseEntity.ok(saved);
    }

    // ✅ Historial de asistencias por cédula
    @GetMapping("/{cedula}")
    public ResponseEntity<?> historial(@PathVariable String cedula) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findByCedula(cedula);

        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }

        Empleado empleado = empleadoOpt.get();
        List<Asistencia> list = asistenciaRepo.findByEmpleado(empleado);

        return ResponseEntity.ok(list);
    }

    // ✅ Listar todas las asistencias (ordenadas por fechaHora descendente)
    @GetMapping
    public List<Asistencia> listarTodas() {
        return asistenciaRepo.findAll(Sort.by(Sort.Direction.DESC, "fechaHora"));
    }
}
