USE control_asistencia;

-- Eliminar primero las tablas hijas (dependientes de empleados)
DROP TABLE IF EXISTS asistencias;
DROP TABLE IF EXISTS turnos;
DROP TABLE IF EXISTS alertas;

-- eliminar la tabla padre
DROP TABLE IF EXISTS empleados;

-- Eliminar usuarios si quieres recrearla desde cero
DROP TABLE IF EXISTS usuarios;

-- Crear tabla empleados
CREATE TABLE empleados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cedula VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(50)
);

-- Crear tabla asistencias
CREATE TABLE asistencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE
);

-- Crear tabla usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Crear tabla alertas
CREATE TABLE alertas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    empleado_id INT NOT NULL,
    nombre_empleado VARCHAR(100) NOT NULL,
    tipo ENUM('TARDANZA','SALIDA_TEMPRANA','INCUMPLIMIENTO') NOT NULL,
    detalle VARCHAR(255),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla turnos
CREATE TABLE turnos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id BIGINT NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME NOT NULL,
    FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE
);


-- Insertar usuario admin con contraseña "admin123"
-- Si ya existe, actualiza la contraseña
INSERT INTO usuarios (usuario, password)
VALUES (
  'admin',
  '$2a$10$JxarsGFQhszzh3FGHxeoveHvRrK7xyajQO4wynZrFk8ketbOmbMIC'
)
ON DUPLICATE KEY UPDATE password = VALUES(password);
