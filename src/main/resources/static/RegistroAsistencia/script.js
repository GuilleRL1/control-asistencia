/* ============================================================
   Control de Asistencia — Lógica de la interfaz
   ============================================================ */

const API = "http://localhost:8080/api";
const API_AUTH = `${API}/auth`;
const API_ASISTENCIAS = `${API}/asistencias`;
const API_EMPLEADOS = `${API}/empleados`;
const API_TURNOS = `${API}/turnos`;
const API_ALERTAS = `${API}/alertas`;
const API_REPORTES = `${API}/reportes`;
const API_USUARIOS = `${API}/usuarios`;
const API_DASHBOARD = `${API}/dashboard`;

const SECCIONES_ADMIN = new Set(["dashboard", "empleados", "turnos", "alertas", "reportes", "usuarios", "ajustes", "historial"]);

// ============ Utilidades ============
const $ = (id) => document.getElementById(id);

function esc(s) {
  return String(s ?? "").replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

function toast(mensaje, tipo = "info") {
  const cont = $("toasts");
  const el = document.createElement("div");
  el.className = `toast toast-${tipo}`;
  el.textContent = mensaje;
  cont.appendChild(el);
  setTimeout(() => {
    el.classList.add("salida-anim");
    setTimeout(() => el.remove(), 300);
  }, 3500);
}

async function leerError(res) {
  try {
    const data = await res.json();
    return data.error || data.message || JSON.stringify(data);
  } catch {
    return await res.text();
  }
}

// ============ Sesión ============
function usuarioLogueado() {
  return sessionStorage.getItem("usuarioLogueado");
}

function actualizarTopbar() {
  const user = usuarioLogueado();
  const nombre = $("usuario-nombre");
  const avatar = $("usuario-avatar");
  const btn = $("btn-sesion");
  if (nombre) nombre.textContent = user || "Invitado";
  if (avatar) avatar.textContent = user ? user.charAt(0).toUpperCase() : "?";
  if (btn) btn.textContent = user ? "Cerrar sesión" : "Iniciar sesión";
}

function alternarSesion() {
  if (usuarioLogueado()) cerrarSesion();
  else navegar("autenticacion");
}

function cerrarSesion() {
  sessionStorage.removeItem("usuarioLogueado");
  toast("Sesión cerrada", "info");
  actualizarTopbar();
  navegar("inicio");
}

function irAdmin() {
  navegar(usuarioLogueado() ? "dashboard" : "autenticacion");
}

// ============ Navegación ============
function navegar(id) {
  if (SECCIONES_ADMIN.has(id) && !usuarioLogueado()) {
    toast("Debes iniciar sesión primero", "warning");
    id = "autenticacion";
  }
  history.pushState({ seccion: id }, "", "#" + id);
  mostrarSeccion(id);
}

function mostrarSeccion(id) {
  if (SECCIONES_ADMIN.has(id) && !usuarioLogueado()) {
    id = "autenticacion";
  }

  document.querySelectorAll(".section").forEach(s => s.classList.add("oculto"));
  const target = $(id);
  if (target) target.classList.remove("oculto");

  const sidebar = $("sidebar");
  sidebar.classList.toggle("oculto", !SECCIONES_ADMIN.has(id));
  sidebar.classList.remove("abierta");

  document.querySelectorAll(".nav-item[data-seccion]").forEach(b =>
    b.classList.toggle("activo", b.dataset.seccion === id));

  if (id === "dashboard") cargarDashboard();
  else if (id === "empleados") cargarEmpleados();
  else if (id === "turnos") cargarTurnos();
  else if (id === "alertas") cargarAlertas();
  else if (id === "usuarios") cargarUsuarios();
  else if (id === "historial") cargarHistorial();
  else if (id === "registro") cargarAsistencias();

  actualizarTopbar();
  actualizarReloj();
}

function toggleSidebar() {
  $("sidebar").classList.toggle("abierta");
}

window.onpopstate = e => {
  if (e.state && e.state.seccion) mostrarSeccion(e.state.seccion);
  else mostrarSeccion("inicio");
};

// ============ Tema y reloj ============
function aplicarTema(tema) {
  document.body.classList.toggle("tema-oscuro", tema === "oscuro");
  const btn = $("btn-tema");
  if (btn) btn.textContent = tema === "oscuro" ? "☀️" : "🌙";
  const sel = $("tema");
  if (sel) sel.value = tema;
}

function alternarTema() {
  const actual = document.body.classList.contains("tema-oscuro") ? "oscuro" : "claro";
  const nuevo = actual === "oscuro" ? "claro" : "oscuro";
  localStorage.setItem("tema", nuevo);
  aplicarTema(nuevo);
}

function actualizarReloj() {
  const formato = localStorage.getItem("formatoHora") || "24";
  const opts = { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: formato === "12" };
  const hora = new Date().toLocaleTimeString("es-EC", opts);
  const reloj = $("reloj");
  if (reloj) reloj.textContent = hora;
  const ha = $("hora-actual");
  if (ha) ha.value = hora;
}

function formatearFecha(fecha) {
  if (!fecha) return "";
  const d = new Date(fecha);
  return isNaN(d) ? fecha : d.toLocaleString("es-EC", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
}

function formatearHora(horaISO) {
  if (!horaISO || horaISO === "-") return "-";
  const formato = localStorage.getItem("formatoHora") || "24";
  const fecha = new Date("1970-01-01T" + horaISO);
  if (isNaN(fecha)) return horaISO;
  return fecha.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", hour12: formato === "12" });
}

function tipoAlerta(tipo) {
  if (tipo === "TARDANZA") return { clase: "tardanza", ico: "⏰" };
  if (tipo === "SALIDA_TEMPRANA") return { clase: "salida", ico: "🚶" };
  return { clase: "incumplimiento", ico: "⚠️" };
}

// ============ Modal ============
function abrirModal(titulo, html) {
  $("modal-titulo").textContent = titulo;
  $("modal-body").innerHTML = html;
  $("modal-overlay").classList.remove("oculto");
}

function cerrarModal() {
  $("modal-overlay").classList.add("oculto");
}

// ============ Login ============
async function login() {
  const usuario = $("usuario").value.trim();
  const contrasena = $("contrasena").value.trim();
  if (!usuario || !contrasena) { toast("Ingresa usuario y contraseña", "warning"); return; }

  try {
    const res = await fetch(`${API_AUTH}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usuario, password: contrasena })
    });
    if (!res.ok) throw new Error(await leerError(res) || "Credenciales inválidas");
    sessionStorage.setItem("usuarioLogueado", usuario);
    toast("Inicio de sesión exitoso", "success");
    actualizarTopbar();
    navegar("dashboard");
  } catch (e) {
    toast(e.message, "error");
  }
}

// ============ Recuperación de contraseña ============
async function solicitarRecuperacion() {
  const email = $("recuperar-email").value.trim();
  if (!email) { toast("Ingresa tu email", "warning"); return; }

  try {
    const res = await fetch(`${API_AUTH}/forgot-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email })
    });
    let data;
    try { data = await res.json(); } catch { data = await res.text(); }
    if (!res.ok) throw new Error(typeof data === "string" ? data : (data.message || "Error"));

    const resultado = $("recuperar-resultado");
    resultado.classList.remove("oculto");

    if (data.resetToken) {
      $("recuperar-token").value = data.resetToken;
      resultado.innerHTML = `
        <strong>📧 Email simulado</strong>
        <p class="muted">Se generó un enlace para <strong>${esc(email)}</strong>. En producción se enviaría por correo; para la demo se muestra aquí:</p>
        <p><a href="${data.resetLink}" target="_blank">${esc(data.resetLink)}</a></p>
        <p class="muted">Token: <code>${esc(data.resetToken)}</code></p>`;
      $("recuperar-paso2").classList.remove("oculto");
      toast("Enlace generado (email simulado)", "success");
    } else {
      resultado.innerHTML = `<p>${esc(data.message || "Si el email está registrado, recibirás un enlace.")}</p>`;
    }
  } catch (e) {
    toast(e.message, "error");
  }
}

async function restablecerContrasena() {
  const token = $("recuperar-token").value.trim();
  const nueva = $("recuperar-nueva").value.trim();
  const confirmar = $("recuperar-confirmar").value.trim();

  if (!token || !nueva || !confirmar) { toast("Completa todos los campos", "warning"); return; }
  if (nueva !== confirmar) { toast("Las contraseñas no coinciden", "warning"); return; }

  try {
    const res = await fetch(`${API_AUTH}/reset-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, nueva })
    });
    const msg = await res.text();
    if (!res.ok) throw new Error(msg || "Error al restablecer");
    toast(msg, "success");
    ["recuperar-email", "recuperar-token", "recuperar-nueva", "recuperar-confirmar"].forEach(id => $(id).value = "");
    $("recuperar-resultado").classList.add("oculto");
    $("recuperar-paso2").classList.add("oculto");
    navegar("autenticacion");
  } catch (e) {
    toast(e.message, "error");
  }
}

// ============ Registro de asistencia ============
async function registrarAsistencia(tipo) {
  const cedula = $("cedula").value.trim();
  if (!cedula) { toast("Ingresa una cédula válida", "warning"); return; }

  try {
    const res = await fetch(`${API_ASISTENCIAS}/${cedula}/${tipo}`, { method: "POST" });
    if (!res.ok) {
      const d = await res.json().catch(() => null);
      throw new Error(d?.error || "No se pudo registrar");
    }
    const data = await res.json();
    toast(`${tipo} registrada para ${data.empleado?.nombre || cedula}`, "success");
    cargarAsistencias();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function cargarAsistencias() {
  const log = $("registro-log");
  if (!log) return;
  try {
    const res = await fetch(API_ASISTENCIAS);
    const asistencias = await res.json();
    if (!asistencias.length) { log.innerHTML = `<div class="vacio">Sin registros todavía</div>`; return; }
    log.innerHTML = `
      <div class="table-wrap">
        <table>
          <thead><tr><th>Cédula</th><th>Empleado</th><th>Tipo</th><th>Fecha</th></tr></thead>
          <tbody>${asistencias.slice(0, 10).map(a => `
            <tr>
              <td>${esc(a.empleado?.cedula || "")}</td>
              <td>${esc(a.empleado?.nombre || "")}</td>
              <td><span class="badge badge-${a.tipo === "entrada" ? "entrada" : "salida-reg"}">${esc(a.tipo)}</span></td>
              <td>${formatearFecha(a.fechaHora)}</td>
            </tr>`).join("")}
          </tbody>
        </table>
      </div>`;
  } catch {
    log.innerHTML = `<div class="vacio">❌ Error al cargar</div>`;
  }
}

// ============ Dashboard ============
async function cargarDashboard() {
  try {
    const res = await fetch(API_DASHBOARD);
    if (!res.ok) throw new Error(await leerError(res));
    const d = await res.json();

    $("kpi-grid").innerHTML =
      kpi("👥", d.totalEmpleados ?? 0, "Empleados") +
      kpi("🟢", d.presentesHoy ?? 0, "Presentes hoy", true) +
      kpi("🔁", d.asistenciasHoy ?? 0, "Registros hoy") +
      kpi("✅", d.entradasHoy ?? 0, "Entradas hoy") +
      kpi("🚪", d.salidasHoy ?? 0, "Salidas hoy") +
      kpi("🚨", d.alertasHoy ?? 0, "Alertas hoy") +
      kpi("⏱️", (d.puntualidadHoy ?? 100) + "%", "Puntualidad") +
      kpi("🕒", d.totalTurnos ?? 0, "Turnos");

    renderChart(d.asistenciaSemanal || []);
    renderAlertasRecientes(d.alertasRecientes || []);
  } catch (e) {
    toast(e.message, "error");
  }
}

function kpi(ico, valor, etiqueta, acento = false) {
  return `<div class="kpi ${acento ? "acento" : ""}">
    <span class="kpi-ico">${ico}</span>
    <div class="kpi-valor">${esc(valor)}</div>
    <div class="kpi-etiqueta">${esc(etiqueta)}</div>
  </div>`;
}

function renderChart(semanal) {
  const cont = $("chart");
  if (!semanal.length) { cont.innerHTML = `<div class="vacio">Sin datos</div>`; return; }
  const max = Math.max(1, ...semanal.flatMap(d => [d.entradas, d.salidas]));
  cont.innerHTML = semanal.map(d => {
    const hE = Math.round(d.entradas / max * 100);
    const hS = Math.round(d.salidas / max * 100);
    const fecha = new Date(d.fecha + "T00:00:00");
    const dia = isNaN(fecha) ? d.fecha : fecha.toLocaleDateString("es-EC", { weekday: "short", day: "2-digit" });
    return `<div class="chart-col">
      <div class="chart-bars">
        <div class="chart-bar entradas" style="height:${hE}%"></div>
        <div class="chart-bar salidas" style="height:${hS}%"></div>
      </div>
      <span class="chart-leyenda">${dia}</span>
    </div>`;
  }).join("");
}

function renderAlertasRecientes(alertas) {
  const cont = $("alertas-recientes");
  if (!alertas.length) { cont.innerHTML = `<div class="vacio">Sin alertas recientes 🎉</div>`; return; }
  cont.innerHTML = alertas.map(a => {
    const t = tipoAlerta(a.tipo);
    return `<div class="alerta-item ${t.clase}">
      <span class="alerta-ico">${t.ico}</span>
      <div class="alerta-cuerpo"><strong>${esc(a.nombreEmpleado)}</strong><small>${formatearFecha(a.fecha)}</small></div>
      <span class="badge badge-${t.clase}">${esc(a.tipo)}</span>
    </div>`;
  }).join("");
}

// ============ Empleados ============
async function cargarEmpleados() {
  const tbody = $("tabla-empleados");
  try {
    const res = await fetch(API_EMPLEADOS);
    const empleados = await res.json();
    const filtro = ($("empleados-busqueda")?.value || "").toLowerCase();
    const lista = empleados.filter(e =>
      (e.nombre || "").toLowerCase().includes(filtro) || (e.cedula || "").includes(filtro));

    if (!lista.length) {
      tbody.innerHTML = `<tr><td colspan="4" class="vacio">No hay empleados</td></tr>`;
      return;
    }
    tbody.innerHTML = lista.map(e => `
      <tr>
        <td>${esc(e.cedula)}</td>
        <td>${esc(e.nombre)}</td>
        <td>${esc(e.telefono || "-")}</td>
        <td><div class="tabla-acciones">
          <button class="btn btn-ghost btn-sm" onclick="verPerfil('${esc(e.cedula)}')" title="Ver perfil">👁️</button>
          <button class="btn btn-ghost btn-sm" onclick="editarEmpleado('${esc(e.cedula)}')" title="Editar">✏️</button>
          <button class="btn btn-danger btn-sm" onclick="eliminarEmpleado('${esc(e.cedula)}')" title="Eliminar">🗑️</button>
        </div></td>
      </tr>`).join("");
  } catch {
    tbody.innerHTML = `<tr><td colspan="4" class="vacio">❌ Error al cargar</td></tr>`;
  }
}

function abrirModalEmpleado(emp) {
  const d = emp || { cedula: "", nombre: "", telefono: "" };
  abrirModal(emp ? "Editar empleado" : "Nuevo empleado", `
    <div class="input-group"><label>Cédula</label><input type="text" id="f-cedula" value="${esc(d.cedula)}" ${emp ? "disabled" : ""}></div>
    <div class="input-group"><label>Nombre</label><input type="text" id="f-nombre" value="${esc(d.nombre)}"></div>
    <div class="input-group"><label>Teléfono</label><input type="text" id="f-telefono" value="${esc(d.telefono)}"></div>
    <div style="display:flex;gap:.6rem;justify-content:flex-end;margin-top:1rem">
      <button class="btn btn-ghost" onclick="cerrarModal()">Cancelar</button>
      <button class="btn btn-primario" onclick="${emp ? `actualizarEmpleado('${esc(d.cedula)}')` : "guardarEmpleado()"}">Guardar</button>
    </div>`);
}

async function guardarEmpleado() {
  const cedula = $("f-cedula").value.trim();
  const nombre = $("f-nombre").value.trim();
  const telefono = $("f-telefono").value.trim();
  if (!cedula || !nombre) { toast("Cédula y nombre son obligatorios", "warning"); return; }

  try {
    const res = await fetch(API_EMPLEADOS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ cedula, nombre, telefono })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Empleado registrado", "success");
    cerrarModal();
    cargarEmpleados();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function editarEmpleado(cedula) {
  try {
    const res = await fetch(`${API_EMPLEADOS}/${cedula}`);
    if (!res.ok) throw new Error(await leerError(res));
    abrirModalEmpleado(await res.json());
  } catch (e) {
    toast(e.message, "error");
  }
}

async function actualizarEmpleado(cedula) {
  const nombre = $("f-nombre").value.trim();
  const telefono = $("f-telefono").value.trim();
  if (!nombre) { toast("El nombre es obligatorio", "warning"); return; }

  try {
    const res = await fetch(`${API_EMPLEADOS}/${cedula}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ cedula, nombre, telefono })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Empleado actualizado", "success");
    cerrarModal();
    cargarEmpleados();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function eliminarEmpleado(cedula) {
  if (!confirm(`¿Eliminar empleado con cédula ${cedula}?`)) return;
  try {
    const res = await fetch(`${API_EMPLEADOS}/${cedula}`, { method: "DELETE" });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Empleado eliminado", "success");
    cargarEmpleados();
  } catch (e) {
    toast(e.message, "error");
  }
}

// ============ Perfil / historial ============
let empleadoActualCedula = null;

function verPerfil(cedula) {
  empleadoActualCedula = cedula;
  navegar("historial");
}

async function cargarHistorial() {
  if (!empleadoActualCedula) return;
  const cont = $("historial-contenido");
  try {
    const res = await fetch(`${API_EMPLEADOS}/${empleadoActualCedula}/resumen`);
    if (!res.ok) throw new Error(await leerError(res));
    const r = await res.json();
    const e = r.empleado;
    const turno = r.turno;
    const asistencias = r.asistencias || [];
    const alertas = r.alertas || [];
    const turnoTxt = turno ? `${turno.horaEntrada} - ${turno.horaSalida}` : "Sin turno asignado";

    const filas = asistencias.slice(0, 10).map(a => `
      <tr>
        <td>${formatearFecha(a.fechaHora)}</td>
        <td><span class="badge badge-${a.tipo === "entrada" ? "entrada" : "salida-reg"}">${esc(a.tipo)}</span></td>
      </tr>`).join("");

    const alertasHtml = alertas.length
      ? alertas.map(a => {
          const t = tipoAlerta(a.tipo);
          return `<div class="alerta-item ${t.clase}">
            <span class="alerta-ico">${t.ico}</span>
            <div class="alerta-cuerpo"><strong>${esc(a.tipo)}</strong><small>${esc(a.detalle)}</small><br><small>${formatearFecha(a.fecha)}</small></div>
          </div>`;
        }).join("")
      : `<div class="vacio">Sin alertas</div>`;

    cont.innerHTML = `
      <div class="card">
        <div class="page-head">
          <h2>${esc(e.nombre)}</h2>
          <span class="badge badge-ok">Cédula ${esc(e.cedula)}</span>
        </div>
        <p class="muted">📞 ${esc(e.telefono || "Sin teléfono")} &nbsp;·&nbsp; 🕒 Turno: <strong>${esc(turnoTxt)}</strong></p>
      </div>
      <div class="grid-2 mt">
        <div class="card">
          <h3>🕐 Asistencias recientes</h3>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Fecha</th><th>Tipo</th></tr></thead>
              <tbody>${filas || `<tr><td colspan="2" class="vacio">Sin registros</td></tr>`}</tbody>
            </table>
          </div>
        </div>
        <div class="card">
          <h3>🚨 Alertas</h3>
          ${alertasHtml}
        </div>
      </div>`;
  } catch (e) {
    cont.innerHTML = `<div class="vacio">❌ ${esc(e.message)}</div>`;
  }
}

// ============ Turnos ============
async function cargarTurnos() {
  const tbody = $("tabla-turnos");
  try {
    const res = await fetch(API_TURNOS);
    const turnos = await res.json();
    if (!turnos.length) { tbody.innerHTML = `<tr><td colspan="5" class="vacio">No hay turnos registrados</td></tr>`; return; }
    tbody.innerHTML = turnos.map(t => `
      <tr>
        <td>${esc(t.idTurno)}</td>
        <td>${esc(t.cedula)}</td>
        <td>${esc(t.nombre)}</td>
        <td>${esc(t.horaEntrada)}</td>
        <td>${esc(t.horaSalida)}</td>
      </tr>`).join("");
  } catch {
    tbody.innerHTML = `<tr><td colspan="5" class="vacio">❌ Error al cargar</td></tr>`;
  }
}

async function guardarTurnoPorCedula() {
  const cedula = $("turno-cedula").value.trim();
  const horaEntrada = $("turno-entrada").value;
  const horaSalida = $("turno-salida").value;
  if (!cedula || !horaEntrada || !horaSalida) { toast("Completa cédula, entrada y salida", "warning"); return; }

  try {
    const res = await fetch(API_TURNOS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ cedula, horaEntrada, horaSalida })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Turno guardado", "success");
    cargarTurnos();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function asignarTurnoMasivo() {
  const horaEntrada = $("turno-entrada").value;
  const horaSalida = $("turno-salida").value;
  if (!horaEntrada || !horaSalida) { toast("Define entrada y salida", "warning"); return; }

  try {
    const res = await fetch(`${API_TURNOS}/masivo`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ horaEntrada, horaSalida })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast(await res.text(), "success");
    cargarTurnos();
  } catch (e) {
    toast(e.message, "error");
  }
}

// ============ Alertas ============
async function cargarAlertas() {
  const cont = $("lista-alertas");
  try {
    const res = await fetch(API_ALERTAS);
    const alertas = await res.json();
    if (!alertas.length) { cont.innerHTML = `<div class="vacio">✅ No hay alertas recientes</div>`; return; }
    cont.innerHTML = alertas.map(a => {
      const t = tipoAlerta(a.tipo);
      return `<div class="alerta-item ${t.clase}">
        <span class="alerta-ico">${t.ico}</span>
        <div class="alerta-cuerpo">
          <strong>${esc(a.nombreEmpleado)}</strong>
          <span class="muted">${esc(a.detalle || "")}</span><br>
          <small>${formatearFecha(a.fecha)}</small>
        </div>
        <span class="badge badge-${t.clase}">${esc(a.tipo)}</span>
      </div>`;
    }).join("");
  } catch {
    cont.innerHTML = `<div class="vacio">❌ Error al cargar alertas</div>`;
  }
}

// ============ Reportes ============
async function cargarReporteAsistencias() {
  const inicio = $("fechaInicio").value;
  const fin = $("fechaFin").value;
  if (!inicio || !fin) { toast("Selecciona el rango de fechas", "warning"); return; }

  try {
    const res = await fetch(`${API_REPORTES}/asistencias?inicio=${inicio}&fin=${fin}`);
    if (!res.ok) throw new Error(await leerError(res));
    const datos = await res.json();
    const tbody = $("tbody-reporte");
    if (!datos.length) {
      tbody.innerHTML = `<tr><td colspan="6" class="vacio">Sin datos en el rango</td></tr>`;
    } else {
      tbody.innerHTML = datos.map(r => `
        <tr>
          <td>${esc(r.cedula)}</td>
          <td>${esc(r.nombre)}</td>
          <td>${esc(r.fecha)}</td>
          <td>${formatearHora(r.horaEntrada)}</td>
          <td>${formatearHora(r.horaSalida)}</td>
          <td>${esc(r.horasTrabajadas)}</td>
        </tr>`).join("");
    }
    const resumen = $("resumen-reporte");
    if (resumen) resumen.textContent = `${datos.length} registro(s)`;
  } catch (e) {
    toast(e.message, "error");
  }
}

function exportarCSV() {
  const filas = document.querySelectorAll("#tbody-reporte tr");
  if (!filas.length) { toast("No hay datos para exportar", "warning"); return; }
  let csv = "Cédula,Nombre,Fecha,Hora Entrada,Hora Salida,Horas Trabajadas\n";
  filas.forEach(tr => {
    const cols = tr.querySelectorAll("td");
    csv += Array.from(cols).map(td => td.innerText).join(",") + "\n";
  });
  const blob = new Blob([csv], { type: "text/csv" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "reporte_asistencias.csv";
  a.click();
  URL.revokeObjectURL(url);
  toast("CSV exportado", "success");
}

// ============ Usuarios ============
async function cargarUsuarios() {
  const tbody = $("tabla-usuarios");
  try {
    const res = await fetch(API_USUARIOS);
    const usuarios = await res.json();
    if (!usuarios.length) { tbody.innerHTML = `<tr><td colspan="3" class="vacio">No hay usuarios</td></tr>`; return; }
    tbody.innerHTML = usuarios.map(u => `
      <tr>
        <td>${esc(u.usuario)}</td>
        <td>${esc(u.email || "-")}</td>
        <td><div class="tabla-acciones">
          <button class="btn btn-ghost btn-sm" onclick="editarUsuario(${u.id})" title="Editar">✏️</button>
          <button class="btn btn-danger btn-sm" onclick="eliminarUsuario(${u.id})" title="Eliminar">🗑️</button>
        </div></td>
      </tr>`).join("");
  } catch {
    tbody.innerHTML = `<tr><td colspan="3" class="vacio">❌ Error al cargar</td></tr>`;
  }
}

function abrirModalUsuario(usr) {
  const d = usr || { usuario: "", email: "" };
  abrirModal(usr ? "Editar usuario" : "Nuevo usuario", `
    <div class="input-group"><label>Usuario</label><input type="text" id="f-usuario" value="${esc(d.usuario)}"></div>
    <div class="input-group"><label>Email</label><input type="email" id="f-email" value="${esc(d.email || "")}"></div>
    <div class="input-group"><label>Contraseña ${usr ? "(opcional)" : ""}</label><input type="password" id="f-password" placeholder="${usr ? "Dejar vacío para no cambiar" : "••••••"}"></div>
    <div style="display:flex;gap:.6rem;justify-content:flex-end;margin-top:1rem">
      <button class="btn btn-ghost" onclick="cerrarModal()">Cancelar</button>
      <button class="btn btn-primario" onclick="${usr ? `actualizarUsuario(${usr.id})` : "guardarUsuario()"}">Guardar</button>
    </div>`);
}

async function guardarUsuario() {
  const usuario = $("f-usuario").value.trim();
  const email = $("f-email").value.trim();
  const password = $("f-password").value;
  if (!usuario || !password) { toast("Usuario y contraseña son obligatorios", "warning"); return; }

  try {
    const res = await fetch(API_USUARIOS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usuario, email, password })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Usuario creado", "success");
    cerrarModal();
    cargarUsuarios();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function editarUsuario(id) {
  try {
    const res = await fetch(API_USUARIOS);
    const lista = await res.json();
    const u = lista.find(x => x.id === id);
    if (u) abrirModalUsuario(u);
  } catch (e) {
    toast(e.message, "error");
  }
}

async function actualizarUsuario(id) {
  const usuario = $("f-usuario").value.trim();
  const email = $("f-email").value.trim();
  const password = $("f-password").value;
  try {
    const res = await fetch(`${API_USUARIOS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usuario, email, password })
    });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Usuario actualizado", "success");
    cerrarModal();
    cargarUsuarios();
  } catch (e) {
    toast(e.message, "error");
  }
}

async function eliminarUsuario(id) {
  if (!confirm("¿Eliminar este usuario?")) return;
  try {
    const res = await fetch(`${API_USUARIOS}/${id}`, { method: "DELETE" });
    if (!res.ok) throw new Error(await leerError(res));
    toast("Usuario eliminado", "success");
    cargarUsuarios();
  } catch (e) {
    toast(e.message, "error");
  }
}

// ============ Ajustes ============
async function guardarAjustes() {
  const tema = $("tema").value;
  const formato = $("formatoHora").value;
  localStorage.setItem("tema", tema);
  localStorage.setItem("formatoHora", formato);
  aplicarTema(tema);
  actualizarReloj();

  const actual = $("pass-actual").value.trim();
  const nueva = $("pass-nueva").value.trim();
  const confirmar = $("pass-confirmar").value.trim();

  if (actual || nueva || confirmar) {
    if (!actual || !nueva || !confirmar) { toast("Completa todos los campos de contraseña", "warning"); return; }
    if (nueva !== confirmar) { toast("Las contraseñas no coinciden", "warning"); return; }
    const usuario = usuarioLogueado();
    try {
      const res = await fetch(`${API_AUTH}/password/${usuario}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ actual, nueva })
      });
      if (!res.ok) throw new Error(await leerError(res));
      toast("Contraseña actualizada", "success");
      ["pass-actual", "pass-nueva", "pass-confirmar"].forEach(id => $(id).value = "");
    } catch (e) {
      toast(e.message, "error");
    }
  } else {
    toast("Ajustes guardados", "success");
  }
}

// ============ Inicialización ============
document.addEventListener("DOMContentLoaded", () => {
  aplicarTema(localStorage.getItem("tema") || "claro");
  const temaSel = $("tema");
  if (temaSel) temaSel.value = localStorage.getItem("tema") || "claro";
  const formatoSel = $("formatoHora");
  if (formatoSel) formatoSel.value = localStorage.getItem("formatoHora") || "24";

  actualizarReloj();
  setInterval(actualizarReloj, 1000);
  actualizarTopbar();

  const btnLogin = $("btn-login");
  if (btnLogin) btnLogin.addEventListener("click", login);

  const overlay = $("modal-overlay");
  if (overlay) overlay.addEventListener("click", e => { if (e.target === overlay) cerrarModal(); });

  // Ruta inicial desde el hash (ej. #recuperar?token=..., #dashboard)
  const hash = window.location.hash || "";
  if (hash.startsWith("#recuperar")) {
    const token = new URLSearchParams(hash.split("?")[1] || "").get("token");
    mostrarSeccion("recuperar");
    if (token) {
      $("recuperar-token").value = token;
      $("recuperar-paso2").classList.remove("oculto");
    }
  } else if (hash.length > 1 && $(hash.slice(1))) {
    mostrarSeccion(hash.slice(1));
  } else {
    mostrarSeccion("inicio");
  }
});
