package com.techforge.control_asistencia.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.repository.EmpleadoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    @Autowired
    private EmpleadoRepository empleadoRepo;

    // 🟢 Crear nuevo empleado con validaciones
    @PostMapping
    public ResponseEntity<?> crearEmpleado(@Valid @RequestBody Empleado empleado) {
        try {
            Optional<Empleado> existente = empleadoRepo.findByCedula(empleado.getCedula());
            if (existente.isPresent()) {
                return ResponseEntity.badRequest().body("⚠️ Ya existe un empleado con esta cédula");
            }
            Empleado nuevo = empleadoRepo.save(empleado);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error al guardar el empleado");
        }
    }

    // 🔵 Obtener todos los empleados
    @GetMapping
    public List<Empleado> listarEmpleados() {
        return empleadoRepo.findAll();
    }

    // 🟡 Obtener empleado por cédula
    @GetMapping("/{cedula}")
    public ResponseEntity<?> obtenerEmpleado(@PathVariable String cedula) {
        Optional<Empleado> empOpt = empleadoRepo.findByCedula(cedula);
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }
        return ResponseEntity.ok(empOpt.get());
    }

    // 🟠 Actualizar empleado por cédula
    @PutMapping("/{cedula}")
    public ResponseEntity<?> actualizarEmpleado(@PathVariable String cedula, @Valid @RequestBody Empleado actualizado) {
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
    public ResponseEntity<?> eliminarEmpleado(@PathVariable String cedula) {
        Optional<Empleado> existenteOpt = empleadoRepo.findByCedula(cedula);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Empleado no encontrado");
        }
        empleadoRepo.delete(existenteOpt.get());
        return ResponseEntity.ok("✅ Empleado eliminado correctamente");
    }
}

