<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>SICIMED - Inicio</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark bg-primary">
  <div class="container"><span class="navbar-brand">SICIMED</span></div>
</nav>
<div class="container py-4">
  <h2>Panel académico (JSP)</h2>
  <p>Perfil BD: <strong>${perfil}</strong></p>
  <p>Usuarios semilla listos. Consumir la API desde Angular o Postman.</p>
  <ul>
    <li>POST /api/auth/login</li>
    <li>GET /api/sedes</li>
    <li>GET /api/especialidades</li>
    <li>GET /api/medicos</li>
    <li>CRUD /api/citas</li>
  </ul>
  <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">Volver</a>
</div>
</body>
</html>
