package com.techforge.control_asistencia.dto;

import java.time.LocalTime;

public class TurnoResponseDTO {
    private Long idTurno;
    private String cedula;
    private String nombre;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    public TurnoResponseDTO(Long idTurno, String cedula, String nombre, LocalTime horaEntrada, LocalTime horaSalida) {
        this.idTurno = idTurno;
        this.cedula = cedula;
        this.nombre = nombre;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
    }

    // Getters y setters
    public Long getIdTurno() { return idTurno; }
    public void setIdTurno(Long idTurno) { this.idTurno = idTurno; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }
}