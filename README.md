# control-asistencia

Este es un proyecto en Java Spring Boot con MySQL para gestionar la asistencia de empleados.

Cómo iniciar el proyecto
- Clonar el repositorio
git clone https://github.com/GuilleRL1/control-asistencia.git
cd control-asistencia
- Configurar la base de datos
Edita el archivo src/main/resources/application.properties con tus credenciales de MySQL:
spring.datasource.url=jdbc:mysql://localhost:3306/control_asistencia
spring.datasource.username=admin
spring.datasource.password=admin123
spring.jpa.hibernate.ddl-auto=update
- Ejecutar el proyecto con Maven
mvn spring-boot:run
- Acceder a la aplicación
Abre en tu navegador la dirección:
http://localhost:8080
- Dirigirse a la carpeta del frontend
El frontend básico está en la carpeta src/main/resources/static.
Allí encontrarás los archivos HTML, CSS y JavaScript que permiten la interacción visual con el sistema.
Puedes abrirlos directamente en tu navegador o modificarlos para personalizar la interfaz.
Requisitos previos
- Java 21
- Maven
- MySQL
Notas
El sistema crea un usuario administrador por defecto:
Usuario: admin
Contraseña: admin123

