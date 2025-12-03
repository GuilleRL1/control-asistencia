const API_AUTH = "http://localhost:8080/api/auth";
// 🌐 Configuración

const API_ASISTENCIAS = "http://localhost:8080/api/asistencias";
const API_EMPLEADOS = "http://localhost:8080/api/empleados";


// Navegación

function navegar(id) {
  // Si intenta ir a administracion sin login, redirige a login
  if (id === "administracion" && !sessionStorage.getItem("usuarioLogueado")) {
    alert("⚠️ Debes iniciar sesión primero");
    mostrarSeccion("autenticacion");
    return;
  }

  history.pushState({ seccion: id }, "", "#" + id);
  mostrarSeccion(id);
}

function mostrarSeccion(id) {
  document.querySelectorAll(".section").forEach(s => s.classList.add("oculto"));
  const target = document.getElementById(id);
  if (target) target.classList.remove("oculto");
}

window.onpopstate = e => {
  if (e.state && e.state.seccion) mostrarSeccion(e.state.seccion);
  else mostrarSeccion("inicio");
};


// 🔐 Login (Real, no simulado)

document.addEventListener("DOMContentLoaded", () => {
  const btnLogin = document.getElementById("btn-login");
  console.log("¿Encontré el botón?", btnLogin);

  if (btnLogin) {
    console.log("Listener de login registrado");
    btnLogin.addEventListener("click", async () => {
      console.log("Click detectado en btn-login");

      const usuario = document.getElementById("usuario").value.trim();
      const contrasena = document.getElementById("contrasena").value.trim();

      if (!usuario || !contrasena) {
        alert("⚠️ Ingresa usuario y contraseña");
        return;
      }

      try {
        const res = await fetch(`${API_AUTH}/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ usuario, password: contrasena })
        });

        if (!res.ok) {
          const msg = await res.text();
          throw new Error(msg);
        }

        sessionStorage.setItem("usuarioLogueado", usuario);
        alert("✅ Inicio de sesión exitoso");
        navegar("administracion");
      } catch (err) {
        alert("❌ " + err.message);
      }
    });
  }
});

// 🧾 Registrar asistencia

async function registrarAsistencia(tipo) {
  const cedula = document.getElementById("cedula").value.trim();
  if (!cedula) {
    alert("⚠️ Debes ingresar una cédula válida");
    return;
  }

  try {
    const res = await fetch(`${API_ASISTENCIAS}/${cedula}/${tipo}`, { method: "POST" });
    if (!res.ok) throw new Error("Error al registrar asistencia");

    const data = await res.json();
    alert(`✅ ${tipo} registrada correctamente para ${data.empleado?.nombre || cedula}`);

    // 🔄 Refrescar tanto el log del registro como el panel admin
    await cargarAsistencias();
    await cargarAsistenciasAdmin();
  } catch (error) {
    console.error(error);
    alert("❌ No se pudo registrar la asistencia. Verifica la conexión al backend.");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const entrada = document.getElementById("btn-entrada");
  const salida = document.getElementById("btn-salida");
  if (entrada) entrada.addEventListener("click", () => registrarAsistencia("entrada"));
  if (salida) salida.addEventListener("click", () => registrarAsistencia("salida"));
  cargarAsistencias();
});


// 📋 Mostrar asistencias

async function cargarAsistencias() {
  const log = document.getElementById("registro-log");
  if (!log) return;

  try {
    const res = await fetch(API_ASISTENCIAS);
    if (!res.ok) throw new Error("Error al cargar asistencias");
    const asistencias = await res.json();

    if (asistencias.length === 0) {
      log.innerHTML = "<strong>No hay registros de asistencia.</strong>";
      return;
    }

    let html = "<strong>📅 Últimas asistencias:</strong><br>";
    asistencias.slice(-10).reverse().forEach(a => {
      const fecha = a.fechaHora ? new Date(a.fechaHora).toLocaleString() : "(sin fecha)";
      html += `${a.empleado?.cedula || a.cedula} — ${a.tipo} — ${fecha}<br>`;
    });
    log.innerHTML = html;
  } catch (error) {
    log.innerHTML = "❌ Error al obtener asistencias.";
  }
}

// AJUSTES
function guardarAjustes() {
  const tema = document.getElementById("tema").value;
  const formatoHora = document.getElementById("formatoHora").value;

  // Guardar en localStorage para persistencia
  localStorage.setItem("tema", tema);
  localStorage.setItem("formatoHora", formatoHora);

  alert("✅ Ajustes guardados");

  // Aplicar cambios inmediatos
  aplicarTema(tema);
  aplicarFormatoHora(formatoHora);
}

function aplicarTema(tema) {
  document.body.className = tema === "oscuro" ? "tema-oscuro" : "tema-claro";
  // Reflejar en el select
  const selectTema = document.getElementById("tema");
  if (selectTema) selectTema.value = tema;
}

function aplicarFormatoHora(formato) {
  // Reflejar en el select
  const selectFormato = document.getElementById("formatoHora");
  if (selectFormato) selectFormato.value = formato;

  // Refrescar el reloj si existe
  if (typeof actualizarHora === "function") {
    actualizarHora();
  }
}

// Inicializar ajustes al cargar la página
document.addEventListener("DOMContentLoaded", () => {
  const temaGuardado = localStorage.getItem("tema") || "claro";
  const formatoGuardado = localStorage.getItem("formatoHora") || "24";

  aplicarTema(temaGuardado);
  aplicarFormatoHora(formatoGuardado);
});

// 🚪 Cerrar sesión
function cerrarSesion() {
  sessionStorage.removeItem("usuarioLogueado");
  navegar("inicio");
  alert("🚪 Sesión cerrada");
}


// 🧭 Menú Administración
function mostrarAdminContenido(seccion) {
  const cont = document.getElementById("contenido-admin");

  if (seccion === "registro") {
    cont.innerHTML = `
      <h3>Registro de Asistencia</h3>
      <p>Consulta y gestiona las asistencias registradas.</p>
      <button class="success" onclick="cargarAsistenciasAdmin()">🔄 Actualizar Lista</button>
      <div id="lista-asistencias"></div>
    `;
    cargarAsistenciasAdmin();

  } else if (seccion === "errores") {
    cont.innerHTML = `
      <h3>Reporte de Errores</h3>
      <p><strong>Fecha:</strong> 15/06/2025<br><strong>Error:</strong> Fallo de conexión</p>
      <p><strong>Fecha:</strong> 14/06/2025<br><strong>Error:</strong> Usuario no encontrado</p>
    `;

  } else if (seccion === "alertas") {
    cargarAlertas(); // Esto carga alertas reales desde el backend

  } else if (seccion === "ajustes") {
    cont.innerHTML = "";
    mostrarSeccion("ajustes");

  } else if (seccion === "usuarios") {
    abrirGestionUsuarios();

  } else if (seccion === "turnos") {
    cont.innerHTML = `
      <h3>🕒 Gestión de Turnos</h3>
      <p>Asigna horarios de entrada y salida a cada empleado.</p>
      <div id="form-turno"></div>
      <div id="lista-turnos"></div>
    `;
    cargarTurnos();
  }
}

// 🕒 Gestión de Turnos
function cargarTurnos() {
  const form = document.getElementById("form-turno");
  const lista = document.getElementById("lista-turnos");

  form.innerHTML = `
    <h4>Asignar Turno</h4>
    <label>Cédula Empleado:</label><input type="text" id="turno-cedula"><br>
    <label>Hora Entrada:</label><input type="time" id="turno-entrada"><br>
    <label>Hora Salida:</label><input type="time" id="turno-salida"><br>
    <button class="success" onclick="guardarTurnoPorCedula()">💾 Guardar Turno</button>
    <button class="warning" onclick="asignarTurnoMasivo()">👥 Asignar a Todo el Personal</button>
    <hr>
  `;

  fetch("http://localhost:8080/api/turnos")
    .then(res => res.json())
    .then(turnos => {
      if (!turnos || turnos.length === 0) {
        lista.innerHTML = "<p>✅ No hay turnos registrados aún.</p>";
        return;
      }

      let html = "<h4>Turnos Registrados</h4><table><tr><th>ID Turno</th><th>Cédula</th><th>Nombre</th><th>Entrada</th><th>Salida</th></tr>";
      turnos.forEach(t => {
        html += `
          <tr>
            <td>${t.idTurno}</td>
            <td>${t.cedula}</td>
            <td>${t.nombre}</td>
            <td>${t.horaEntrada}</td>
            <td>${t.horaSalida}</td>
          </tr>
        `;
      });
      html += "</table>";
      lista.innerHTML = html;
    });
}

// ✅ Guardar turno por cédula
function guardarTurnoPorCedula() {
  const cedula = document.getElementById("turno-cedula").value.trim();
  const entrada = document.getElementById("turno-entrada").value;
  const salida = document.getElementById("turno-salida").value;

  if (!cedula || !entrada || !salida) {
    alert("⚠️ Completa cédula, hora de entrada y salida.");
    return;
  }

  const dto = { cedula, horaEntrada: entrada, horaSalida: salida };

  fetch("http://localhost:8080/api/turnos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto)
  })
  .then(async res => {
    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || "Error al guardar turno");
    }
    return res.json();
  })
  .then(() => {
    alert("✅ Turno guardado correctamente");
    cargarTurnos();
  })
  .catch(err => {
    console.error(err);
    alert("❌ " + err.message);
  });
}

// ✅ Asignar turno masivo a todo el personal
function asignarTurnoMasivo() {
  const entrada = document.getElementById("turno-entrada").value;
  const salida = document.getElementById("turno-salida").value;

  if (!entrada || !salida) {
    alert("⚠️ Define hora de entrada y salida para asignación masiva.");
    return;
  }

  const dto = { horaEntrada: entrada, horaSalida: salida };

  fetch("http://localhost:8080/api/turnos/masivo", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto)
  })
  .then(async res => {
    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || "Error en asignación masiva");
    }
    return res.text();
  })
  .then(msg => {
    alert("✅ " + msg);
    cargarTurnos();
  })
  .catch(err => {
    console.error(err);
    alert("❌ " + err.message);
  });
}

// 📋 Tabla asistencias (admin)

async function cargarAsistenciasAdmin() {
  const div = document.getElementById("lista-asistencias");
  if (!div) return;

  try {
    const res = await fetch(API_ASISTENCIAS);
    const asistencias = await res.json();

    if (asistencias.length === 0) {
      div.innerHTML = "<p>No hay registros aún.</p>";
      return;
    }

    let html = "<table><tr><th>Cédula</th><th>Empleado</th><th>Tipo</th><th>Fecha</th></tr>";
    asistencias.slice(-20).reverse().forEach(a => {
      const fecha = a.fechaHora ? new Date(a.fechaHora).toLocaleString() : "";
      html += `<tr><td>${a.empleado?.cedula}</td><td>${a.empleado?.nombre}</td><td>${a.tipo}</td><td>${fecha}</td></tr>`;
    });
    html += "</table>";

    div.innerHTML = html;
  } catch {
    div.innerHTML = "<p>Error al cargar asistencias.</p>";
  }
}


// 👥 Gestión de usuarios (CRUD)
async function abrirGestionUsuarios() {
  const cont = document.getElementById("contenido-admin");
  cont.innerHTML = `
    <h3>Gestión de Empleados</h3>
    <button class="success" onclick="mostrarFormularioEmpleado()">➕ Nuevo Empleado</button>
    <div id="lista-empleados"></div>
  `;
  cargarEmpleados();
}

async function cargarEmpleados() {
  const div = document.getElementById("lista-empleados");
  try {
    const res = await fetch(API_EMPLEADOS);
    const empleados = await res.json();

    if (empleados.length === 0) {
      div.innerHTML = "<p>No hay empleados registrados.</p>";
      return;
    }

    let html = "<table><tr><th>Cédula</th><th>Nombre</th><th>Teléfono</th><th>Acciones</th></tr>";
    empleados.forEach(e => {
      html += `
        <tr>
          <td>${e.cedula}</td>
          <td>${e.nombre}</td>
          <td>${e.telefono || "-"}</td>
          <td>
            <button onclick="editarEmpleado('${e.cedula}')">✏️ Editar</button>
            <button class="error" onclick="eliminarEmpleado('${e.cedula}')">🗑 Eliminar</button>
          </td>
        </tr>`;
    });
    html += "</table>";
    div.innerHTML = html;
  } catch {
    div.innerHTML = "<p>Error al cargar empleados.</p>";
  }
}

function mostrarFormularioEmpleado() {
  const cont = document.getElementById("contenido-admin");
  cont.innerHTML = `
    <h3>Nuevo Empleado</h3>
    <div class="input-group"><label>Cédula</label><input type="text" id="cedula-empleado"></div>
    <div class="input-group"><label>Nombre</label><input type="text" id="nombre-empleado"></div>
    <div class="input-group"><label>Teléfono</label><input type="text" id="telefono-empleado"></div>
    <button type="button" class="success" onclick="guardarEmpleado()">💾 Guardar</button>
    <button class="btn-volver" onclick="abrirGestionUsuarios()">⬅ Volver</button>
  `;
}

async function guardarEmpleado() {
  const cedula = document.getElementById("cedula-empleado").value.trim();
  const nombre = document.getElementById("nombre-empleado").value.trim();
  const telefono = document.getElementById("telefono-empleado").value.trim();

  if (!cedula || !nombre) {
    alert("⚠️ Debes ingresar al menos cédula y nombre");
    return;
  }

  try {
    const res = await fetch(API_EMPLEADOS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ cedula, nombre, telefono })
    });

    if (!res.ok) {
      const errorMsg = await res.text();
      throw new Error(errorMsg);
    }

    alert("✅ Empleado registrado correctamente");

    // limpiar inputs
    document.getElementById("cedula-empleado").value = "";
    document.getElementById("nombre-empleado").value = "";
    document.getElementById("telefono-empleado").value = "";

    abrirGestionUsuarios();
  } catch (err) {
    alert("❌ " + err.message);
  }
}

async function eliminarEmpleado(cedula) {
  if (!confirm(`¿Eliminar empleado con cédula ${cedula}?`)) return;

  try {
    const res = await fetch(`${API_EMPLEADOS}/${cedula}`, { method: "DELETE" });
    if (!res.ok) throw new Error();
    alert("🗑 Empleado eliminado");
    cargarEmpleados();
  } catch {
    alert("❌ No se pudo eliminar el empleado");
  }
}
// Función de cargar alertas
async function cargarAlertas() {
  const cont = document.getElementById("contenido-admin");

  try {
    const res = await fetch("http://localhost:8080/api/alertas");
    if (!res.ok) throw new Error("Error al cargar alertas");

    const alertas = await res.json();

    if (alertas.length === 0) {
      cont.innerHTML = "<h3>🚨 Alertas de Asistencia</h3><p>✅ No hay alertas recientes</p>";
      return;
    }

    let html = "<h3>🚨 Alertas de Asistencia</h3><p>Aquí se muestran las incidencias recientes de los empleados.</p>";

    alertas.forEach(a => {
      let clase = "";
      if (a.tipo === "TARDANZA") clase = "tardanza";
      if (a.tipo === "SALIDA_TEMPRANA") clase = "salida-temprana";
      if (a.tipo === "INCUMPLIMIENTO") clase = "incumplimiento";

      html += `
        <div class="alerta ${clase}">
          <strong>${a.nombreEmpleado}</strong> - ${a.detalle}<br>
          <small>${new Date(a.fecha).toLocaleString()}</small>
        </div>
      `;
    });

    html += `<button class="success" onclick="cargarAlertas()">🔄 Actualizar Alertas</button>`;
    cont.innerHTML = html;

  } catch (err) {
    cont.innerHTML = `<p class="error">❌ ${err.message}</p>`;
  }
}