package com.techforge.control_asistencia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empleadoId;

    private String nombreEmpleado;

    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;

    private String detalle;

    private LocalDateTime fecha;

    // --- Constructores ---
    public Alerta() {}

    // ✅ Constructor para crear alertas rápidas desde AsistenciaController
    public Alerta(Long empleadoId, String nombreEmpleado, TipoAlerta tipo, String detalle) {
        this.empleadoId = empleadoId;
        this.nombreEmpleado = nombreEmpleado;
        this.tipo = tipo;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public TipoAlerta getTipo() { return tipo; }
    public void setTipo(TipoAlerta tipo) { this.tipo = tipo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    // --- Enum interno ---
    public enum TipoAlerta {
        TARDANZA,
        SALIDA_TEMPRANA,
        INCUMPLIMIENTO
    }
}