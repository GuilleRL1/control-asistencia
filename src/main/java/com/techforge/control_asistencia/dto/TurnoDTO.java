package com.techforge.control_asistencia.dto;

import java.time.LocalTime;

public class TurnoDTO {
    private String cedula;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    // Getters y setters
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }
}