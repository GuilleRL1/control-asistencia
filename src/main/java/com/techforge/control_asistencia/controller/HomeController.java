package com.techforge.control_asistencia.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "✅ API de Control de Asistencia funcionando correctamente.<br>"
             + "Prueba las rutas:<br>"
             + "- /api/empleados<br>"
             + "- /api/asistencias";
    }
}

