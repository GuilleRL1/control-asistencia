package com.techforge.control_asistencia.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Cédula: obligatoria, única, solo números, entre 6 y 10 dígitos
    @Column(nullable = false, unique = true)
    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]+$", message = "La cédula solo puede contener números")
    @Size(min = 6, max = 10, message = "La cédula debe tener entre 6 y 10 dígitos")
    private String cedula;

    // ✅ Nombre: obligatorio
    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    // ✅ Teléfono: solo números, máximo 10 dígitos
    @Pattern(regexp = "^[0-9]+$", message = "El teléfono solo puede contener números")
    @Size(max = 10, message = "El teléfono no puede superar los 10 dígitos")
    private String telefono;

    public Empleado() {}

    public Empleado(String cedula, String nombre, String telefono) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    // Getters y setters
    public Long getId() { return id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}