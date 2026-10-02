-- ============================================================
--  SICIMED - Esquema MySQL 8 / MariaDB 10.6+
--  Sistema Web Distribuido para la Gestion de Citas Medicas
--  UPN - Soluciones Web y Aplicaciones Distribuidas
--
--  Con el backend en Spring Boot, ddl-auto=update crea las tablas
--  automaticamente. Este script existe para crear la base vacia
--  y para poder revisar o modificar el esquema a mano.
--
--  Uso:
--    mysql -u root -p < 01_create_sicimed_mysql.sql
--  O desde MySQL Workbench: abrir el archivo y ejecutar.
-- ============================================================

CREATE DATABASE IF NOT EXISTS sicimed
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sicimed;

-- ---------- catalogos ----------
CREATE TABLE IF NOT EXISTS sedes (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  nombre    VARCHAR(100) NOT NULL,
  direccion VARCHAR(200) NULL,
  telefono  VARCHAR(30)  NULL,
  activo    BIT(1)       NOT NULL DEFAULT b'1',
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS especialidades (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_especialidades_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS medicamentos (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(150) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo      BIT(1)       NOT NULL DEFAULT b'1',
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------- seguridad ----------
CREATE TABLE IF NOT EXISTS usuarios (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  username        VARCHAR(80)  NOT NULL,
  password        VARCHAR(120) NOT NULL COMMENT 'Hash BCrypt, nunca texto plano',
  nombre_completo VARCHAR(120) NOT NULL,
  email           VARCHAR(120) NULL,
  rol             VARCHAR(20)  NOT NULL COMMENT 'PACIENTE, MEDICO, RECEPCIONISTA, ADMIN',
  activo          BIT(1)       NOT NULL DEFAULT b'1',
  PRIMARY KEY (id),
  CONSTRAINT uq_usuarios_username UNIQUE (username)
) ENGINE=InnoDB;

-- ---------- personas del dominio ----------
CREATE TABLE IF NOT EXISTS medicos (
  id             BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id     BIGINT      NOT NULL,
  especialidad_id BIGINT     NOT NULL,
  sede_id        BIGINT      NOT NULL,
  cmp            VARCHAR(30) NULL,
  activo         BIT(1)      NOT NULL DEFAULT b'1',
  PRIMARY KEY (id),
  CONSTRAINT uq_medicos_usuario UNIQUE (usuario_id),
  CONSTRAINT fk_medicos_usuario     FOREIGN KEY (usuario_id)     REFERENCES usuarios(id),
  CONSTRAINT fk_medicos_especialidad FOREIGN KEY (especialidad_id) REFERENCES especialidades(id),
  CONSTRAINT fk_medicos_sede        FOREIGN KEY (sede_id)        REFERENCES sedes(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pacientes (
  id                BIGINT      NOT NULL AUTO_INCREMENT,
  usuario_id        BIGINT      NOT NULL,
  dni               VARCHAR(20) NULL,
  fecha_nacimiento  DATE        NULL,
  telefono          VARCHAR(30) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_pacientes_usuario UNIQUE (usuario_id),
  CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- ---------- operacion ----------
CREATE TABLE IF NOT EXISTS citas (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  medico_id    BIGINT       NOT NULL,
  paciente_id  BIGINT       NOT NULL,
  sede_id      BIGINT       NOT NULL,
  fecha        DATE         NOT NULL,
  hora         TIME         NOT NULL,
  estado       VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, ATENDIDO, CANCELADO',
  motivo       VARCHAR(500) NULL,
  diagnostico  VARCHAR(1000) NULL,
  version      BIGINT       NOT NULL DEFAULT 0 COMMENT 'Control de concurrencia optimista (@Version)',
  PRIMARY KEY (id),
  -- Indice de apoyo para la consulta de disponibilidad y choques de horario
  INDEX idx_citas_medico_fecha (medico_id, fecha, hora),
  CONSTRAINT fk_citas_medico   FOREIGN KEY (medico_id)   REFERENCES medicos(id),
  CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
  CONSTRAINT fk_citas_sede     FOREIGN KEY (sede_id)     REFERENCES sedes(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recetas (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  cita_id      BIGINT        NOT NULL,
  indicaciones VARCHAR(2000) NOT NULL,
  medicamentos TEXT          NULL COMMENT 'Lista de medicamentos del catalogo en JSON',
  creado_en    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_recetas_cita UNIQUE (cita_id),
  CONSTRAINT fk_recetas_cita FOREIGN KEY (cita_id) REFERENCES citas(id)
) ENGINE=InnoDB;

-- ============================================================
--  Datos de prueba
--
--  Las contrasenas se guardan con hash BCrypt. El backend tambien
--  carga esta semilla solo si la tabla usuarios esta vacia
--  (DatosSemilla), asi que no es obligatorio ejecutar este bloque.
-- ============================================================

INSERT INTO sedes (nombre, direccion, telefono) VALUES
  ('Sede Centro', 'Av. Grau 123, Trujillo', '044-111111'),
  ('Sede Norte',  'Av. Espana 456, Trujillo', '044-222222');

INSERT INTO especialidades (nombre, descripcion) VALUES
  ('Medicina General', 'Atencion primaria'),
  ('Pediatria',        'Atencion infantil'),
  ('Ginecologia',      'Salud de la mujer');

INSERT INTO medicamentos (nombre) VALUES
  ('Paracetamol 500mg'),
  ('Amoxicilina 500mg'),
  ('Ibuprofeno 400mg'),
  ('Loratadina 10mg'),
  ('Omeprazol 20mg'),
  ('Azitromicina 500mg');
