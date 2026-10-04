package com.techforge.control_asistencia.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.techforge.control_asistencia.model.Alerta;
import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.model.Turno;
import com.techforge.control_asistencia.repository.AlertaRepository;
import com.techforge.control_asistencia.repository.AsistenciaRepository;
import com.techforge.control_asistencia.repository.EmpleadoRepository;
import com.techforge.control_asistencia.repository.TurnoRepository;

@Service
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final EmpleadoRepository empleadoRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final TurnoRepository turnoRepo;
    private final AlertaRepository alertaRepo;

    public DashboardService(EmpleadoRepository empleadoRepo, AsistenciaRepository asistenciaRepo,
                            TurnoRepository turnoRepo, AlertaRepository alertaRepo) {
        this.empleadoRepo = empleadoRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.turnoRepo = turnoRepo;
        this.alertaRepo = alertaRepo;
    }

    public Map<String, Object> resumen() {
        LocalDate hoy = LocalDate.now(ZONE);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("totalEmpleados", empleadoRepo.count());
        data.put("totalTurnos", turnoRepo.count());

        // Asistencias de hoy
        List<Asistencia> asistenciasHoyList = asistenciaRepo.findByFechaHoraBetween(inicioDia, finDia);
        long entradasHoy = asistenciasHoyList.stream()
                .filter(a -> "entrada".equalsIgnoreCase(a.getTipo())).count();
        long salidasHoy = asistenciasHoyList.stream()
                .filter(a -> "salida".equalsIgnoreCase(a.getTipo())).count();

        data.put("asistenciasHoy", asistenciasHoyList.size());
        data.put("entradasHoy", entradasHoy);
        data.put("salidasHoy", salidasHoy);

        // Presentes ahora: empleados con entrada de hoy y sin salida registrada
        Set<Long> conEntrada = asistenciasHoyList.stream()
                .filter(a -> "entrada".equalsIgnoreCase(a.getTipo()))
                .map(a -> a.getEmpleado().getId())
                .collect(Collectors.toSet());
        Set<Long> conSalida = asistenciasHoyList.stream()
                .filter(a -> "salida".equalsIgnoreCase(a.getTipo()))
                .map(a -> a.getEmpleado().getId())
                .collect(Collectors.toSet());
        long presentesHoy = conEntrada.stream().filter(id -> !conSalida.contains(id)).count();
        data.put("presentesHoy", presentesHoy);

        // Puntualidad: entradas de hoy que no superan la hora del turno
        long entradasConTurno = 0;
        long puntuales = 0;
        for (Asistencia a : asistenciasHoyList) {
            if (!"entrada".equalsIgnoreCase(a.getTipo())) {
                continue;
            }
            Turno turno = turnoRepo.findByEmpleadoId(a.getEmpleado().getId());
            if (turno == null || turno.getHoraEntrada() == null) {
                continue;
            }
            entradasConTurno++;
            if (!a.getFechaHora().toLocalTime().isAfter(turno.getHoraEntrada())) {
                puntuales++;
            }
        }
        double puntualidad = entradasConTurno > 0 ? (100.0 * puntuales / entradasConTurno) : 100.0;
        data.put("puntualidadHoy", Math.round(puntualidad * 10.0) / 10.0);

        // Alertas de hoy y recientes
        data.put("alertasHoy", alertaRepo.findByFechaBetween(inicioDia, finDia).size());
        List<Alerta> alertasRecientes = alertaRepo.findAll(Sort.by(Sort.Direction.DESC, "fecha"))
                .stream().limit(5).collect(Collectors.toList());
        data.put("alertasRecientes", alertasRecientes);

        // Asistencia de los últimos 7 días (para el gráfico)
        LocalDateTime semanaInicio = hoy.minusDays(6).atStartOfDay();
        List<Asistencia> semanales = asistenciaRepo.findByFechaHoraBetween(semanaInicio, finDia);

        Map<LocalDate, int[]> porDia = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            porDia.put(hoy.minusDays(i), new int[]{0, 0});
        }
        for (Asistencia a : semanales) {
            LocalDate dia = a.getFechaHora().toLocalDate();
            if (porDia.containsKey(dia)) {
                if ("entrada".equalsIgnoreCase(a.getTipo())) {
                    porDia.get(dia)[0]++;
                } else if ("salida".equalsIgnoreCase(a.getTipo())) {
                    porDia.get(dia)[1]++;
                }
            }
        }

        List<Map<String, Object>> asistenciaSemanal = new ArrayList<>();
        for (Map.Entry<LocalDate, int[]> e : porDia.entrySet()) {
            Map<String, Object> dia = new LinkedHashMap<>();
            dia.put("fecha", e.getKey().toString());
            dia.put("entradas", e.getValue()[0]);
            dia.put("salidas", e.getValue()[1]);
            asistenciaSemanal.add(dia);
        }
        data.put("asistenciaSemanal", asistenciaSemanal);

        return data;
    }
}
