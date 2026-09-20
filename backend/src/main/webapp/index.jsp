<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>SICIMED</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
  <h1 class="mb-3">SICIMED</h1>
  <p class="lead">Sistema Web Distribuido para la Gestión de Citas Médicas — Policlínico San Rafael</p>
  <a class="btn btn-primary" href="<%=request.getContextPath()%>/inicio">Ir al panel JSP</a>
  <p class="mt-4 text-muted">API REST JSON disponible bajo <code>/api/**</code> (consumida por Angular en :4200).</p>
</div>
</body>
</html>
