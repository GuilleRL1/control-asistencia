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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techforge.control_asistencia.model.Alerta;
import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Empleado;
import com.techforge.control_asistencia.model.Turno;
import com.techforge.control_asistencia.repository.AlertaRepository;
import com.techforge.control_asistencia.repository.AsistenciaRepository;
import com.techforge.control_asistencia.repository.EmpleadoRepository;
import com.techforge.control_asistencia.repository.TurnoRepository;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    @Autowired
    private AsistenciaRepository asistenciaRepo;

    @Autowired
    private EmpleadoRepository empleadoRepo;

    @Autowired
    private TurnoRepository turnoRepo;

    @Autowired
    private AlertaRepository alertaRepo;

    // 🟢 Registrar asistencia por cédula y tipo (entrada/salida)
    @PostMapping("/{cedula}/{tipo}")
    public ResponseEntity<?> registrar(@PathVariable String cedula, @PathVariable String tipo) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findByCedula(cedula);

        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Empleado no encontrado"));
        }

        if (!("entrada".equalsIgnoreCase(tipo) || "salida".equalsIgnoreCase(tipo))) {
            return ResponseEntity.badRequest().body(Map.of("error", "El tipo debe ser 'entrada' o 'salida'"));
        }

        Empleado empleado = empleadoOpt.get();

        // ✅ Obtener asistencias del día actual
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        List<Asistencia> registrosDia = asistenciaRepo.findByEmpleadoAndFechaHoraBetween(empleado, inicioDia, finDia);

        if ("entrada".equalsIgnoreCase(tipo)) {
            boolean yaTieneEntrada = registrosDia.stream().anyMatch(a -> "entrada".equalsIgnoreCase(a.getTipo()));
            if (yaTieneEntrada) {
                return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Ya existe una entrada registrada para este día"));
            }
        }

        if ("salida".equalsIgnoreCase(tipo)) {
            boolean tieneEntrada = registrosDia.stream().anyMatch(a -> "entrada".equalsIgnoreCase(a.getTipo()));
            if (!tieneEntrada) {
                return ResponseEntity.badRequest().body(Map.of("error", "⚠️ No se puede registrar salida sin entrada previa"));
            }
            boolean yaTieneSalida = registrosDia.stream().anyMatch(a -> "salida".equalsIgnoreCase(a.getTipo()));
            if (yaTieneSalida) {
                return ResponseEntity.badRequest().body(Map.of("error", "⚠️ Ya existe una salida registrada para este día"));
            }
        }

        // ✅ Registrar asistencia
        Asistencia nueva = new Asistencia(empleado, null, tipo.toLowerCase());
        Asistencia saved = asistenciaRepo.save(nueva);

        // ✅ Buscar turno del empleado y generar alerta si hay novedad
        Turno turno = turnoRepo.findByEmpleadoId(empleado.getId());
        LocalDateTime ahora = saved.getFechaHora();

        if (turno != null) {
            if ("entrada".equalsIgnoreCase(tipo)) {
                if (ahora.toLocalTime().isAfter(turno.getHoraEntrada())) {
                    alertaRepo.save(new Alerta(
                        empleado.getId(),
                        empleado.getNombre(),
                        Alerta.TipoAlerta.TARDANZA,
                        "Llegada tarde: " + ahora.toLocalTime()
                    ));
                }
            }
            if ("salida".equalsIgnoreCase(tipo)) {
                if (ahora.toLocalTime().isBefore(turno.getHoraSalida())) {
                    alertaRepo.save(new Alerta(
                        empleado.getId(),
                        empleado.getNombre(),
                        Alerta.TipoAlerta.SALIDA_TEMPRANA,
                        "Salida antes de tiempo: " + ahora.toLocalTime()
                    ));
                }
            }
        }

        return ResponseEntity.ok(saved);
    }

    // 🔵 Historial de asistencias por cédula
    @GetMapping("/{cedula}")
    public ResponseEntity<?> historial(@PathVariable String cedula) {
        Optional<Empleado> empleadoOpt = empleadoRepo.findByCedula(cedula);

        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Empleado no encontrado"));
        }

        Empleado empleado = empleadoOpt.get();
        List<Asistencia> list = asistenciaRepo.findByEmpleado(empleado);

        return ResponseEntity.ok(list);
    }

    // 🟠 Listar todas las asistencias (ordenadas por fechaHora descendente)
    @GetMapping
    public List<Asistencia> listarTodas() {
        return asistenciaRepo.findAll(Sort.by(Sort.Direction.DESC, "fechaHora"));
    }
}