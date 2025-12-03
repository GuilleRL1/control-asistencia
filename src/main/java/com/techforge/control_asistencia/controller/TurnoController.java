package com.techforge.control_asistencia.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.dto.TurnoDTO;
import com.techforge.control_asistencia.dto.TurnoResponseDTO;
import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.model.Turno;
import com.techforge.control_asistencia.repository.EmpleadoRepository;
import com.techforge.control_asistencia.repository.TurnoRepository;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "http://localhost:63342") // permite peticiones desde tu frontend
public class TurnoController {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    // ✅ Listar todos los turnos
    @GetMapping
public List<TurnoResponseDTO> listarTurnos() {
    List<Turno> turnos = turnoRepository.findAll();
    return turnos.stream().map(t -> {
        return empleadoRepository.findById(t.getEmpleadoId())
            .map(emp -> new TurnoResponseDTO(
                t.getId(),
                emp.getCedula(),
                emp.getNombre(),
                t.getHoraEntrada(),
                t.getHoraSalida()
            ))
            .orElse(new TurnoResponseDTO(
                t.getId(),
                "N/A",
                "Empleado no encontrado",
                t.getHoraEntrada(),
                t.getHoraSalida()
            ));
    }).toList();
}

    // ✅ Crear/actualizar turno por cédula (solo si el empleado existe)
    @PostMapping
    public ResponseEntity<?> crearTurnoPorCedula(@RequestBody TurnoDTO dto) {
        if (dto.getCedula() == null || dto.getCedula().isBlank()
                || dto.getHoraEntrada() == null || dto.getHoraSalida() == null) {
            return ResponseEntity.badRequest().body("Datos incompletos: cédula, horaEntrada, horaSalida");
        }

        Optional<Empleado> empleadoOpt = empleadoRepository.findByCedula(dto.getCedula().trim());
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empleado no encontrado en la base de datos");
        }

        Empleado empleado = empleadoOpt.get();
        Turno existente = turnoRepository.findByEmpleadoId(empleado.getId());

        Turno turno = (existente != null) ? existente : new Turno();
        turno.setEmpleadoId(empleado.getId());
        turno.setHoraEntrada(dto.getHoraEntrada());
        turno.setHoraSalida(dto.getHoraSalida());

        Turno guardado = turnoRepository.save(turno);
        return ResponseEntity.ok(guardado);
    }

    // ✅ Asignar/actualizar el mismo turno a todo el personal existente
    @PostMapping("/masivo")
    public ResponseEntity<?> asignarTurnoMasivo(@RequestBody TurnoDTO dto) {
        if (dto.getHoraEntrada() == null || dto.getHoraSalida() == null) {
            return ResponseEntity.badRequest().body("Debes definir hora de entrada y salida");
        }

        List<Empleado> empleados = empleadoRepository.findAll();
        empleados.forEach(emp -> {
            Turno existente = turnoRepository.findByEmpleadoId(emp.getId());
            Turno turno = (existente != null) ? existente : new Turno();
            turno.setEmpleadoId(emp.getId());
            turno.setHoraEntrada(dto.getHoraEntrada());
            turno.setHoraSalida(dto.getHoraSalida());
            turnoRepository.save(turno);
        });

        return ResponseEntity.ok("Turno asignado/actualizado a todo el personal existente");
    }

    // ✅ Actualizar turno por ID (mantener compatibilidad con tu frontend actual)
    @PutMapping("/{id}")
    public ResponseEntity<Turno> actualizarTurno(@PathVariable Long id, @RequestBody Turno turno) {
        turno.setId(id);
        return ResponseEntity.ok(turnoRepository.save(turno));
    }
}