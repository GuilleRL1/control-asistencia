package com.techforge.control_asistencia.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techforge.control_asistencia.dto.ReporteAsistenciaDTO;
import com.techforge.control_asistencia.model.Asistencia;
import com.techforge.control_asistencia.repository.AsistenciaRepository;

@Service
public class ReporteService {

    @Autowired
    private AsistenciaRepository asistenciaRepo;

    public List<ReporteAsistenciaDTO> generarAsistencias(String inicio, String fin) {
        LocalDate fechaInicio = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);

        // ✅ Traer todas las asistencias en el rango usando fechaHora
        List<Asistencia> asistencias = asistenciaRepo.findByFechaHoraBetween(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59)
        );

        // ✅ Agrupar por empleado + fecha
        Map<String, List<Asistencia>> agrupado = asistencias.stream()
                .collect(Collectors.groupingBy(a -> a.getEmpleado().getCedula() + "_" + a.getFechaHora().toLocalDate()));

        List<ReporteAsistenciaDTO> reporte = new ArrayList<>();

        for (Map.Entry<String, List<Asistencia>> entry : agrupado.entrySet()) {
            List<Asistencia> registros = entry.getValue();

            // Buscar entrada más temprana y salida más tardía
            Optional<Asistencia> entradaOpt = registros.stream()
                    .filter(a -> "entrada".equalsIgnoreCase(a.getTipo()))
                    .min(Comparator.comparing(Asistencia::getFechaHora));

            Optional<Asistencia> salidaOpt = registros.stream()
                    .filter(a -> "salida".equalsIgnoreCase(a.getTipo()))
                    .max(Comparator.comparing(Asistencia::getFechaHora));

            String horaEntrada = entradaOpt.map(a -> a.getFechaHora().toLocalTime().toString()).orElse("-");
            String horaSalida = salidaOpt.map(a -> a.getFechaHora().toLocalTime().toString()).orElse("-");
            String horasTrabajadas = "-";

            if (entradaOpt.isPresent() && salidaOpt.isPresent()) {
                Duration duracion = Duration.between(
                        entradaOpt.get().getFechaHora(),
                        salidaOpt.get().getFechaHora()
                );
                long horas = duracion.toHours();
                long minutos = duracion.toMinutesPart();
                horasTrabajadas = horas + "h " + minutos + "m";
            }

            Asistencia muestra = registros.get(0);
            reporte.add(new ReporteAsistenciaDTO(
                    muestra.getEmpleado().getCedula(),
                    muestra.getEmpleado().getNombre(),
                    muestra.getFechaHora().toLocalDate().toString(),
                    horaEntrada,
                    horaSalida,
                    horasTrabajadas
            ));
        }

        return reporte;
    }
}