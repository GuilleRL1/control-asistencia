package com.techforge.control_asistencia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    private String tipo; // "entrada" o "salida"

    public Asistencia() {}

    public Asistencia(Empleado empleado, LocalDateTime fechaHora, String tipo) {
        this.empleado = empleado;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    // getters y setters
    public Long getId() { return id; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

