/*
  SICIMED — script para SSMS 22 / SQL Server Express
  Instancia: localhost\SQLEXPRESS
  Auth: Windows (recomendado) o SQL Login

  Cómo ejecutar en SSMS 22:
  1. Conectar a localhost\SQLEXPRESS
  2. Abrir este archivo
  3. Ejecutar (F5) completo
  4. Verificar: USE SICIMED; SELECT * FROM usuarios;

  Alternativa sqlcmd:
  sqlcmd -S "localhost\SQLEXPRESS" -E -C -i "D:\UPN\sicimed\backend\sql\01_create_sicimed.sql"
*/

IF DB_ID(N'SICIMED') IS NULL
BEGIN
    CREATE DATABASE SICIMED;
END
GO

USE SICIMED;
GO

IF OBJECT_ID(N'dbo.recetas', N'U') IS NOT NULL DROP TABLE dbo.recetas;
IF OBJECT_ID(N'dbo.citas', N'U') IS NOT NULL DROP TABLE dbo.citas;
IF OBJECT_ID(N'dbo.pacientes', N'U') IS NOT NULL DROP TABLE dbo.pacientes;
IF OBJECT_ID(N'dbo.medicos', N'U') IS NOT NULL DROP TABLE dbo.medicos;
IF OBJECT_ID(N'dbo.especialidades', N'U') IS NOT NULL DROP TABLE dbo.especialidades;
IF OBJECT_ID(N'dbo.sedes', N'U') IS NOT NULL DROP TABLE dbo.sedes;
IF OBJECT_ID(N'dbo.usuarios', N'U') IS NOT NULL DROP TABLE dbo.usuarios;
GO

CREATE TABLE dbo.usuarios (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  username NVARCHAR(80) NOT NULL UNIQUE,
  password NVARCHAR(120) NOT NULL,
  nombre_completo NVARCHAR(120) NOT NULL,
  email NVARCHAR(120) NULL,
  rol NVARCHAR(20) NOT NULL,
  activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.sedes (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  nombre NVARCHAR(100) NOT NULL,
  direccion NVARCHAR(200) NULL,
  telefono NVARCHAR(30) NULL,
  activo BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.especialidades (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  nombre NVARCHAR(100) NOT NULL UNIQUE,
  descripcion NVARCHAR(255) NULL
);

CREATE TABLE dbo.medicos (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  usuario_id BIGINT NOT NULL UNIQUE,
  especialidad_id BIGINT NOT NULL,
  sede_id BIGINT NOT NULL,
  cmp NVARCHAR(30) NULL,
  activo BIT NOT NULL DEFAULT 1,
  CONSTRAINT fk_medicos_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
  CONSTRAINT fk_medicos_esp FOREIGN KEY (especialidad_id) REFERENCES dbo.especialidades(id),
  CONSTRAINT fk_medicos_sede FOREIGN KEY (sede_id) REFERENCES dbo.sedes(id)
);

CREATE TABLE dbo.pacientes (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  usuario_id BIGINT NOT NULL UNIQUE,
  dni NVARCHAR(20) NULL,
  fecha_nacimiento DATE NULL,
  telefono NVARCHAR(30) NULL,
  CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id)
);

CREATE TABLE dbo.citas (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  medico_id BIGINT NOT NULL,
  paciente_id BIGINT NOT NULL,
  sede_id BIGINT NOT NULL,
  fecha DATE NOT NULL,
  hora TIME NOT NULL,
  estado NVARCHAR(20) NOT NULL,
  motivo NVARCHAR(500) NULL,
  diagnostico NVARCHAR(1000) NULL,
  CONSTRAINT fk_citas_medico FOREIGN KEY (medico_id) REFERENCES dbo.medicos(id),
  CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES dbo.pacientes(id),
  CONSTRAINT fk_citas_sede FOREIGN KEY (sede_id) REFERENCES dbo.sedes(id)
);

CREATE TABLE dbo.recetas (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  cita_id BIGINT NOT NULL UNIQUE,
  indicaciones NVARCHAR(2000) NOT NULL,
  medicamentos NVARCHAR(MAX) NULL,
  creado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  CONSTRAINT fk_recetas_cita FOREIGN KEY (cita_id) REFERENCES dbo.citas(id)
);
GO

-- Seed (hashes BCrypt jBCrypt $2a$10$ ...)
-- Contraseñas: admin123 / recep123 / medico123 / paciente123
INSERT INTO dbo.usuarios(username,password,nombre_completo,email,rol,activo) VALUES
(N'admin', N'$2a$10$yva6nVfOAU/IdlTBp4VrpuBuhyGo2TzMr5xsTDChAMzDHaO324A3K', N'Administrador SICIMED', N'admin@sicimed.pe', N'ADMIN', 1),
(N'recepcion', N'$2a$10$LZGnNPlVKDpyQc02ph.Ea.i.LJPbe89B.CBSmmdiAkhulouOuPJJq', N'Ana Recepcionista', N'recepcion@sicimed.pe', N'RECEPCIONISTA', 1),
(N'medico1', N'$2a$10$mlwnHSZokQ9.9qiwpkEyfuLWjLfEXqUSFySnjCX5lqcUmazIw3R.W', N'Dr. Carlos Mendoza', N'medico1@sicimed.pe', N'MEDICO', 1),
(N'medico2', N'$2a$10$mlwnHSZokQ9.9qiwpkEyfuLWjLfEXqUSFySnjCX5lqcUmazIw3R.W', N'Dra. Lucía Ramírez', N'medico2@sicimed.pe', N'MEDICO', 1),
(N'medico3', N'$2a$10$mlwnHSZokQ9.9qiwpkEyfuLWjLfEXqUSFySnjCX5lqcUmazIw3R.W', N'Dr. Pedro Vargas', N'medico3@sicimed.pe', N'MEDICO', 1),
(N'paciente1', N'$2a$10$3cE.ZrByE9LAPYzMAEMBuuhYqYUGZwHCICITxYqNPZt2CllmCHxHW', N'María Pérez', N'paciente1@sicimed.pe', N'PACIENTE', 1);

INSERT INTO dbo.sedes(nombre,direccion,telefono,activo) VALUES
(N'Sede Centro', N'Av. Grau 123, Trujillo', N'044-111111', 1),
(N'Sede Norte', N'Av. España 456, Trujillo', N'044-222222', 1);

INSERT INTO dbo.especialidades(nombre,descripcion) VALUES
(N'Medicina General', N'Atención primaria'),
(N'Pediatría', N'Atención infantil'),
(N'Ginecología', N'Salud de la mujer');

INSERT INTO dbo.medicos(usuario_id,especialidad_id,sede_id,cmp,activo)
SELECT u.id, e.id, s.id, v.cmp, 1
FROM (VALUES
  (N'medico1', N'Medicina General', N'Sede Centro', N'CMP-12345'),
  (N'medico2', N'Pediatría', N'Sede Norte', N'CMP-67890'),
  (N'medico3', N'Ginecología', N'Sede Centro', N'CMP-11223')
) AS v(username, esp, sede, cmp)
JOIN dbo.usuarios u ON u.username = v.username
JOIN dbo.especialidades e ON e.nombre = v.esp
JOIN dbo.sedes s ON s.nombre = v.sede;

INSERT INTO dbo.pacientes(usuario_id, dni, fecha_nacimiento, telefono)
SELECT id, N'71234567', CAST(N'1995-05-20' AS DATE), N'999888777'
FROM dbo.usuarios WHERE username = N'paciente1';

-- Cita de ejemplo
INSERT INTO dbo.citas(medico_id, paciente_id, sede_id, fecha, hora, estado, motivo)
SELECT m.id, p.id, m.sede_id, CAST(GETDATE() AS DATE), CAST(N'10:00' AS TIME), N'PENDIENTE', N'Control general'
FROM dbo.medicos m
CROSS JOIN dbo.pacientes p
WHERE m.cmp = N'CMP-12345';
GO

PRINT N'SICIMED listo. Usuarios: admin/admin123, recepcion/recep123, medico1/medico123, paciente1/paciente123';
GO


-- ===== Extensión RF-12: catálogo medicamentos (admin) =====
IF OBJECT_ID(N'dbo.medicamentos', N'U') IS NULL
CREATE TABLE dbo.medicamentos (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  nombre NVARCHAR(150) NOT NULL,
  descripcion NVARCHAR(255) NULL,
  activo BIT NOT NULL DEFAULT 1
);
GO

-- Ampliar columna de ítems JSON en recetas (sin romper RecetaDao)
IF COL_LENGTH('dbo.recetas', 'medicamentos') IS NOT NULL
ALTER TABLE dbo.recetas ALTER COLUMN medicamentos NVARCHAR(MAX) NULL;
GO
