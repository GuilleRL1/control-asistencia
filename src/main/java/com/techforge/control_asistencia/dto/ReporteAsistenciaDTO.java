package com.techforge.control_asistencia.dto;

public class ReporteAsistenciaDTO {
    private String cedula;
    private String nombre;
    private String fecha;
    private String horaEntrada;
    private String horaSalida;
    private String horasTrabajadas;

    public ReporteAsistenciaDTO() {}

    public ReporteAsistenciaDTO(String cedula, String nombre, String fecha,
                                String horaEntrada, String horaSalida, String horasTrabajadas) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.horasTrabajadas = horasTrabajadas;
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }

    public String getHoraSalida() { return horaSalida; }
    public void setHoraSalida(String horaSalida) { this.horaSalida = horaSalida; }

    public String getHorasTrabajadas() { return horasTrabajadas; }
    public void setHorasTrabajadas(String horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }
}