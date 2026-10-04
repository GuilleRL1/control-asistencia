package com.techforge.control_asistencia.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.repository.AlertaRepository;
import com.techforge.control_asistencia.repository.AsistenciaRepository;
import com.techforge.control_asistencia.repository.EmpleadoRepository;
import com.techforge.control_asistencia.repository.TurnoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepo;
    private final TurnoRepository turnoRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final AlertaRepository alertaRepo;

    public EmpleadoController(EmpleadoRepository empleadoRepo, TurnoRepository turnoRepo,
                              AsistenciaRepository asistenciaRepo, AlertaRepository alertaRepo) {
        this.empleadoRepo = empleadoRepo;
        this.turnoRepo = turnoRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.alertaRepo = alertaRepo;
    }

    // 🟢 Crear nuevo empleado con validaciones
    @PostMapping
    public ResponseEntity<Object> crearEmpleado(@Valid @RequestBody Empleado empleado) {
        if (empleadoRepo.findByCedula(empleado.getCedula()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ Ya existe un empleado con esta cédula");
        }
        Empleado nuevo = empleadoRepo.save(empleado);
        return ResponseEntity.ok(nuevo);
    }

    // 🔵 Obtener todos los empleados
    @GetMapping
    public List<Empleado> listarEmpleados() {
        return empleadoRepo.findAll();
    }

    // 🟡 Obtener empleado por cédula
    @GetMapping("/{cedula}")
    public ResponseEntity<Object> obtenerEmpleado(@PathVariable String cedula) {
        Optional<Empleado> empOpt = empleadoRepo.findByCedula(cedula);
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }
        return ResponseEntity.ok(empOpt.get());
    }

    // 📇 Resumen completo del empleado: datos + turno + asistencias + alertas
    @GetMapping("/{cedula}/resumen")
    public ResponseEntity<Object> resumenEmpleado(@PathVariable String cedula) {
        Optional<Empleado> empOpt = empleadoRepo.findByCedula(cedula);
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }

        Empleado empleado = empOpt.get();
        List<Asistencia> asistencias = asistenciaRepo.findByEmpleado(empleado);
        asistencias.sort(Comparator.comparing(Asistencia::getFechaHora).reversed());

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("empleado", empleado);
        resumen.put("turno", turnoRepo.findByEmpleadoId(empleado.getId()));
        resumen.put("asistencias", asistencias);
        resumen.put("alertas", alertaRepo.findByEmpleadoId(empleado.getId()));
        return ResponseEntity.ok(resumen);
    }

    // 🟠 Actualizar empleado por cédula
    @PutMapping("/{cedula}")
    public ResponseEntity<Object> actualizarEmpleado(@PathVariable String cedula, @Valid @RequestBody Empleado actualizado) {
        Optional<Empleado> existenteOpt = empleadoRepo.findByCedula(cedula);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }
        Empleado existente = existenteOpt.get();
        existente.setNombre(actualizado.getNombre());
        existente.setTelefono(actualizado.getTelefono());
        empleadoRepo.save(existente);
        return ResponseEntity.ok(existente);
    }

    // 🔴 Eliminar empleado
    @DeleteMapping("/{cedula}")
    public ResponseEntity<Object> eliminarEmpleado(@PathVariable String cedula) {
        Optional<Empleado> existenteOpt = empleadoRepo.findByCedula(cedula);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }
        empleadoRepo.delete(existenteOpt.get());
        return ResponseEntity.ok("✅ Empleado eliminado correctamente");
    }
}
