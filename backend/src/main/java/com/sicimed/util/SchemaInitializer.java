package com.sicimed.util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Crea tablas e inserta datos semilla al arrancar la app (T-SQL o H2/MySQL). */
public final class SchemaInitializer {

    private static volatile boolean done = false;

    private SchemaInitializer() {}

    public static synchronized void ensureInitialized() {
        if (done) return;
        try (Connection conn = DbConnection.getConnection(); Statement st = conn.createStatement()) {
            if (DbConnection.isSqlServer()) {
                createSqlServer(st);
            } else {
                createH2Compatible(st);
            }

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    seed(conn);
                }
            }
            seedMedicamentosIfEmpty(conn);
            done = true;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar el esquema: " + e.getMessage(), e);
        }
    }

    private static void createSqlServer(Statement st) throws Exception {
        st.execute("""
            IF OBJECT_ID(N'dbo.usuarios', N'U') IS NULL
            CREATE TABLE dbo.usuarios (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              username NVARCHAR(80) NOT NULL UNIQUE,
              password NVARCHAR(120) NOT NULL,
              nombre_completo NVARCHAR(120) NOT NULL,
              email NVARCHAR(120) NULL,
              rol NVARCHAR(20) NOT NULL,
              activo BIT NOT NULL DEFAULT 1
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.sedes', N'U') IS NULL
            CREATE TABLE dbo.sedes (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              nombre NVARCHAR(100) NOT NULL,
              direccion NVARCHAR(200) NULL,
              telefono NVARCHAR(30) NULL,
              activo BIT NOT NULL DEFAULT 1
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.especialidades', N'U') IS NULL
            CREATE TABLE dbo.especialidades (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              nombre NVARCHAR(100) NOT NULL UNIQUE,
              descripcion NVARCHAR(255) NULL
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.medicos', N'U') IS NULL
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
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.pacientes', N'U') IS NULL
            CREATE TABLE dbo.pacientes (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              usuario_id BIGINT NOT NULL UNIQUE,
              dni NVARCHAR(20) NULL,
              fecha_nacimiento DATE NULL,
              telefono NVARCHAR(30) NULL,
              CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id)
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.citas', N'U') IS NULL
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
            )
            """);
        st.execute("""
            IF OBJECT_ID(N'dbo.recetas', N'U') IS NULL
            CREATE TABLE dbo.recetas (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              cita_id BIGINT NOT NULL UNIQUE,
              indicaciones NVARCHAR(2000) NOT NULL,
              medicamentos NVARCHAR(MAX) NULL,
              creado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
              CONSTRAINT fk_recetas_cita FOREIGN KEY (cita_id) REFERENCES dbo.citas(id)
            )
            """);

        st.execute("""
            IF OBJECT_ID(N'dbo.medicamentos', N'U') IS NULL
            CREATE TABLE dbo.medicamentos (
              id BIGINT IDENTITY(1,1) PRIMARY KEY,
              nombre NVARCHAR(150) NOT NULL,
              descripcion NVARCHAR(255) NULL,
              activo BIT NOT NULL DEFAULT 1
            )
            """);
        // Ampliar columna medicamentos para JSON de ítems de receta
        st.execute("""
            IF COL_LENGTH('dbo.recetas', 'medicamentos') IS NOT NULL
               AND COL_LENGTH('dbo.recetas', 'medicamentos') < 4000
            ALTER TABLE dbo.recetas ALTER COLUMN medicamentos NVARCHAR(MAX) NULL
            """);
    }

    private static void createH2Compatible(Statement st) throws Exception {
        st.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              username VARCHAR(80) NOT NULL UNIQUE,
              password VARCHAR(120) NOT NULL,
              nombre_completo VARCHAR(120) NOT NULL,
              email VARCHAR(120),
              rol VARCHAR(20) NOT NULL,
              activo BOOLEAN DEFAULT TRUE
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS sedes (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              nombre VARCHAR(100) NOT NULL,
              direccion VARCHAR(200),
              telefono VARCHAR(30),
              activo BOOLEAN DEFAULT TRUE
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS especialidades (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              nombre VARCHAR(100) NOT NULL UNIQUE,
              descripcion VARCHAR(255)
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS medicos (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              usuario_id BIGINT NOT NULL UNIQUE,
              especialidad_id BIGINT NOT NULL,
              sede_id BIGINT NOT NULL,
              cmp VARCHAR(30),
              activo BOOLEAN DEFAULT TRUE,
              FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
              FOREIGN KEY (especialidad_id) REFERENCES especialidades(id),
              FOREIGN KEY (sede_id) REFERENCES sedes(id)
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS pacientes (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              usuario_id BIGINT NOT NULL UNIQUE,
              dni VARCHAR(20),
              fecha_nacimiento DATE,
              telefono VARCHAR(30),
              FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS citas (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              medico_id BIGINT NOT NULL,
              paciente_id BIGINT NOT NULL,
              sede_id BIGINT NOT NULL,
              fecha DATE NOT NULL,
              hora TIME NOT NULL,
              estado VARCHAR(20) NOT NULL,
              motivo VARCHAR(500),
              diagnostico VARCHAR(1000),
              FOREIGN KEY (medico_id) REFERENCES medicos(id),
              FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
              FOREIGN KEY (sede_id) REFERENCES sedes(id)
            )
            """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS recetas (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              cita_id BIGINT NOT NULL UNIQUE,
              indicaciones VARCHAR(2000) NOT NULL,
              medicamentos VARCHAR(4000),
              creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY (cita_id) REFERENCES citas(id)
            )
            """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS medicamentos (
              id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
              nombre VARCHAR(150) NOT NULL,
              descripcion VARCHAR(255),
              activo BOOLEAN DEFAULT TRUE
            )
            """);
    }

    private static void seed(Connection conn) throws Exception {
        String adminPass = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String recepPass = BCrypt.hashpw("recep123", BCrypt.gensalt());
        String medicoPass = BCrypt.hashpw("medico123", BCrypt.gensalt());
        String pacPass = BCrypt.hashpw("paciente123", BCrypt.gensalt());

        insertUser(conn, "admin", adminPass, "Administrador SICIMED", "admin@sicimed.pe", "ADMIN");
        insertUser(conn, "recepcion", recepPass, "Ana Recepcionista", "recepcion@sicimed.pe", "RECEPCIONISTA");
        long medicoUserId = insertUser(conn, "medico1", medicoPass, "Dr. Carlos Mendoza", "medico1@sicimed.pe", "MEDICO");
        long medicoUserId2 = insertUser(conn, "medico2", medicoPass, "Dra. Lucía Ramírez", "medico2@sicimed.pe", "MEDICO");
        long pacUserId = insertUser(conn, "paciente1", pacPass, "María Pérez", "paciente1@sicimed.pe", "PACIENTE");

        long sede1 = insertSede(conn, "Sede Centro", "Av. Grau 123, Trujillo", "044-111111");
        long sede2 = insertSede(conn, "Sede Norte", "Av. España 456, Trujillo", "044-222222");

        long esp1 = insertEsp(conn, "Medicina General", "Atención primaria");
        long esp2 = insertEsp(conn, "Pediatría", "Atención infantil");
        long esp3 = insertEsp(conn, "Ginecología", "Salud de la mujer");

        insertMedico(conn, medicoUserId, esp1, sede1, "CMP-12345");
        insertMedico(conn, medicoUserId2, esp2, sede2, "CMP-67890");
        long medicoUserId3 = insertUser(conn, "medico3", medicoPass, "Dr. Pedro Vargas", "medico3@sicimed.pe", "MEDICO");
        insertMedico(conn, medicoUserId3, esp3, sede1, "CMP-11223");

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pacientes(usuario_id, dni, fecha_nacimiento, telefono) VALUES (?,?,?,?)")) {
            ps.setLong(1, pacUserId);
            ps.setString(2, "71234567");
            ps.setDate(3, java.sql.Date.valueOf("1995-05-20"));
            ps.setString(4, "999888777");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicamentos(nombre,descripcion,activo) VALUES (?,?,1)")) {
            String[][] meds = {
                {"Paracetamol 500 mg", "Analgésico / antipirético"},
                {"Ibuprofeno 400 mg", "Antiinflamatorio"},
                {"Amoxicilina 500 mg", "Antibiótico"},
                {"Loratadina 10 mg", "Antihistamínico"},
                {"Omeprazol 20 mg", "Inhibidor de bomba de protones"}
            };
            for (String[] row : meds) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static long insertUser(Connection conn, String u, String p, String n, String e, String r) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO usuarios(username,password,nombre_completo,email,rol,activo) VALUES (?,?,?,?,?,1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ps.setString(3, n);
            ps.setString(4, e);
            ps.setString(5, r);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static long insertSede(Connection conn, String n, String d, String t) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO sedes(nombre,direccion,telefono,activo) VALUES (?,?,?,1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n);
            ps.setString(2, d);
            ps.setString(3, t);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static long insertEsp(Connection conn, String n, String d) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO especialidades(nombre,descripcion) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n);
            ps.setString(2, d);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static void insertMedico(Connection conn, long uid, long esp, long sede, String cmp) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicos(usuario_id,especialidad_id,sede_id,cmp,activo) VALUES (?,?,?,?,1)")) {
            ps.setLong(1, uid);
            ps.setLong(2, esp);
            ps.setLong(3, sede);
            ps.setString(4, cmp);
            ps.executeUpdate();
        }
    }

    private static void seedMedicamentosIfEmpty(Connection conn) throws Exception {
        int count = -1;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM medicamentos")) {
            rs.next();
            count = rs.getInt(1);
        } catch (Exception e) {
            return; // tabla aún no disponible
        }
        if (count > 0) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO medicamentos(nombre,descripcion,activo) VALUES (?,?,1)")) {
            String[][] meds = {
                {"Paracetamol 500 mg", "Analgésico / antipirético"},
                {"Ibuprofeno 400 mg", "Antiinflamatorio"},
                {"Amoxicilina 500 mg", "Antibiótico"},
                {"Loratadina 10 mg", "Antihistamínico"},
                {"Omeprazol 20 mg", "Inhibidor de bomba de protones"}
            };
            for (String[] row : meds) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
