package com.techforge.control_asistencia.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techforge.control_asistencia.model.Alerta;
import com.techforge.control_asistencia.model.Alerta.TipoAlerta;
import com.techforge.control_asistencia.model.Turno;
import com.techforge.control_asistencia.repository.AlertaRepository;
import com.techforge.control_asistencia.repository.TurnoRepository;

@Service
public class AsistenciaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    public void registrarEntrada(Long empleadoId, LocalDateTime horaEntradaReal, String nombreEmpleado) {
        Turno turno = turnoRepository.findByEmpleadoId(empleadoId);

        if (turno != null && horaEntradaReal.toLocalTime().isAfter(turno.getHoraEntrada())) {
            Alerta alerta = new Alerta();
            alerta.setEmpleadoId(empleadoId);
            alerta.setNombreEmpleado(nombreEmpleado);
            alerta.setTipo(TipoAlerta.TARDANZA);
            alerta.setDetalle("Llegó a las " + horaEntradaReal.toLocalTime() +
                              ", turno " + turno.getHoraEntrada());
            alerta.setFecha(LocalDateTime.now());
            alertaRepository.save(alerta);
        }
    }

    public void registrarSalida(Long empleadoId, LocalDateTime horaSalidaReal, String nombreEmpleado) {
        Turno turno = turnoRepository.findByEmpleadoId(empleadoId);

        if (turno != null && horaSalidaReal.toLocalTime().isBefore(turno.getHoraSalida())) {
            Alerta alerta = new Alerta();
            alerta.setEmpleadoId(empleadoId);
            alerta.setNombreEmpleado(nombreEmpleado);
            alerta.setTipo(TipoAlerta.SALIDA_TEMPRANA);
            alerta.setDetalle("Salió a las " + horaSalidaReal.toLocalTime() +
                              ", turno " + turno.getHoraSalida());
            alerta.setFecha(LocalDateTime.now());
            alertaRepository.save(alerta);
        }
    }
}
