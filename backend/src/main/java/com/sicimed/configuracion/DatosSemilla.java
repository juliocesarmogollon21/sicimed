package com.sicimed.configuracion;

import com.sicimed.modelo.Cita;
import com.sicimed.modelo.Especialidad;
import com.sicimed.modelo.EstadoCita;
import com.sicimed.modelo.Medico;
import com.sicimed.modelo.Medicamento;
import com.sicimed.modelo.Paciente;
import com.sicimed.modelo.Receta;
import com.sicimed.modelo.Rol;
import com.sicimed.modelo.Sede;
import com.sicimed.modelo.Usuario;
import com.sicimed.repositorio.CitaRepositorio;
import com.sicimed.repositorio.EspecialidadRepositorio;
import com.sicimed.repositorio.MedicoRepositorio;
import com.sicimed.repositorio.MedicamentoRepositorio;
import com.sicimed.repositorio.PacienteRepositorio;
import com.sicimed.repositorio.RecetaRepositorio;
import com.sicimed.repositorio.SedeRepositorio;
import com.sicimed.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DatosSemilla implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosSemilla.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final SedeRepositorio sedeRepositorio;
    private final EspecialidadRepositorio especialidadRepositorio;
    private final MedicoRepositorio medicoRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final MedicamentoRepositorio medicamentoRepositorio;
    private final CitaRepositorio citaRepositorio;
    private final RecetaRepositorio recetaRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final EstadoSemilla estadoSemilla;

    public DatosSemilla(UsuarioRepositorio usuarioRepositorio,
                        SedeRepositorio sedeRepositorio,
                        EspecialidadRepositorio especialidadRepositorio,
                        MedicoRepositorio medicoRepositorio,
                        PacienteRepositorio pacienteRepositorio,
                        MedicamentoRepositorio medicamentoRepositorio,
                        CitaRepositorio citaRepositorio,
                        RecetaRepositorio recetaRepositorio,
                        PasswordEncoder passwordEncoder,
                        EstadoSemilla estadoSemilla) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.sedeRepositorio = sedeRepositorio;
        this.especialidadRepositorio = especialidadRepositorio;
        this.medicoRepositorio = medicoRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.medicamentoRepositorio = medicamentoRepositorio;
        this.citaRepositorio = citaRepositorio;
        this.recetaRepositorio = recetaRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.estadoSemilla = estadoSemilla;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.count() > 0) {
            log.info("Base de datos con datos existentes: no se carga la semilla.");
            estadoSemilla.marcarListo();
            return;
        }

        Sede sedeCentro = crearSede("Sede Centro", "Av. Grau 123, Trujillo", "044-111111");
        sedeRepositorio.save(sedeCentro);
        Sede sedeNorte = crearSede("Sede Norte", "Av. Espana 456, Trujillo", "044-222222");
        sedeRepositorio.save(sedeNorte);

        Especialidad general = crearEspecialidad("Medicina General", "Atencion primaria");
        especialidadRepositorio.save(general);
        Especialidad pediatria = crearEspecialidad("Pediatria", "Atencion infantil");
        especialidadRepositorio.save(pediatria);
        Especialidad ginecologia = crearEspecialidad("Ginecologia", "Salud de la mujer");
        especialidadRepositorio.save(ginecologia);

        Usuario admin = crearUsuario("admin", "admin123", "Administrador SICIMED", "admin@sicimed.pe", Rol.ADMIN);
        usuarioRepositorio.save(admin);
        Usuario recepcion = crearUsuario("recepcion", "recep123", "Ana Recepcionista", "recepcion@sicimed.pe", Rol.RECEPCIONISTA);
        usuarioRepositorio.save(recepcion);

        Usuario medico1 = crearUsuario("medico1", "medico123", "Dr. Carlos Mendoza", "medico1@sicimed.pe", Rol.MEDICO);
        usuarioRepositorio.save(medico1);
        Usuario medico2 = crearUsuario("medico2", "medico123", "Dra. Lucia Ramirez", "medico2@sicimed.pe", Rol.MEDICO);
        usuarioRepositorio.save(medico2);
        Usuario medico3 = crearUsuario("medico3", "medico123", "Dr. Pedro Vargas", "medico3@sicimed.pe", Rol.MEDICO);
        usuarioRepositorio.save(medico3);

        Medico drMendoza = crearMedico(medico1, general, sedeCentro, "CMP-12345");
        medicoRepositorio.save(drMendoza);
        Medico draRamirez = crearMedico(medico2, pediatria, sedeNorte, "CMP-67890");
        medicoRepositorio.save(draRamirez);
        Medico drVargas = crearMedico(medico3, ginecologia, sedeCentro, "CMP-11223");
        medicoRepositorio.save(drVargas);

        Usuario uPaciente1 = crearUsuario("paciente1", "paciente123", "Maria Perez", "paciente1@sicimed.pe", Rol.PACIENTE);
        usuarioRepositorio.save(uPaciente1);
        Usuario uPaciente2 = crearUsuario("paciente2", "paciente123", "Luis Alvarez", "paciente2@sicimed.pe", Rol.PACIENTE);
        usuarioRepositorio.save(uPaciente2);
        Usuario uPaciente3 = crearUsuario("paciente3", "paciente123", "Carmen Rojas", "paciente3@sicimed.pe", Rol.PACIENTE);
        usuarioRepositorio.save(uPaciente3);
        Usuario uPaciente4 = crearUsuario("paciente4", "paciente123", "Jorge Diaz", "paciente4@sicimed.pe", Rol.PACIENTE);
        usuarioRepositorio.save(uPaciente4);

        Paciente maria = crearPaciente(uPaciente1, "71234567", "999888777", 1995, 5, 20);
        pacienteRepositorio.save(maria);
        Paciente luis = crearPaciente(uPaciente2, "70112233", "988777666", 1988, 11, 3);
        pacienteRepositorio.save(luis);
        Paciente carmen = crearPaciente(uPaciente3, "74558899", "977666555", 2001, 3, 14);
        pacienteRepositorio.save(carmen);
        Paciente jorge = crearPaciente(uPaciente4, "70990011", "966555444", 1979, 7, 29);
        pacienteRepositorio.save(jorge);

        for (String nombre : List.of("Paracetamol 500mg", "Amoxicilina 500mg", "Ibuprofeno 400mg",
                "Loratadina 10mg", "Omeprazol 20mg", "Azitromicina 500mg")) {
            Medicamento medicamento = new Medicamento();
            medicamento.setNombre(nombre);
            medicamento.setActivo(true);
            medicamentoRepositorio.save(medicamento);
        }

        cargarCitas(drMendoza, draRamirez, drVargas, sedeCentro, sedeNorte,
                maria, luis, carmen, jorge);

        log.info("Datos semilla cargados. Usuarios: admin/admin123, recepcion/recep123, "
                + "medico1/medico123, paciente1/paciente123. Citas de ejemplo: {}",
                citaRepositorio.count());
        estadoSemilla.marcarListo();
    }

    private void cargarCitas(Medico general, Medico pediatra, Medico ginecologo,
                             Sede centro, Sede norte,
                             Paciente maria, Paciente luis, Paciente carmen, Paciente jorge) {
        LocalDate hoy = LocalDate.now();

        Cita c1 = crearCita(general, maria, centro, hoy.minusDays(12), "08:00",
                "Control de presion arterial", EstadoCita.ATENDIDO,
                "Presion controlada en 130/80. Continuar dieta baja en sodio.");
        guardarReceta(c1, "Controlar la presion dos veces por semana.",
                "[{\"medicamento\":\"Losartan 50mg\",\"dosis\":\"1 comprimido\",\"frecuencia\":\"cada 24 horas\"}]");

        Cita c2 = crearCita(pediatra, carmen, norte, hoy.minusDays(9), "09:30",
                "Control de crecimiento infantil", EstadoCita.ATENDIDO,
                "Peso y talla dentro de los percentiles esperados para la edad.");
        guardarReceta(c2, "Continuar con la albumina clinica y la vitamina D.",
                "[{\"medicamento\":\"Multivitaminico infantil\",\"dosis\":\"5 mL\",\"frecuencia\":\"una vez al dia\"},"
                        + "{\"medicamento\":\"Vitamina D 400 UI\",\"dosis\":\"1 gota\",\"frecuencia\":\"una vez al dia\"}]");

        Cita c3 = crearCita(ginecologo, luis, centro, hoy.minusDays(6), "11:00",
                "Consulta de control anual", EstadoCita.ATENDIDO,
                "Examenes dentro de lo esperado. Proxima revision en seis meses.");
        guardarReceta(c3, "Mantener la medicacion habitual.",
                "[{\"medicamento\":\"Omeprazol 20mg\",\"dosis\":\"1 capsula\",\"frecuencia\":\"en ayunas\"}]");

        Cita c4 = crearCita(general, jorge, centro, hoy.minusDays(4), "16:00",
                "Dolor lumbar mecanico", EstadoCita.CANCELADO, null);
        c4.setDiagnostico("Cancelada por el paciente.");

        crearCita(general, maria, centro, hoy.minusDays(2), "10:00",
                "Seguimiento de laboratorio", EstadoCita.PENDIENTE, null);

        crearCita(general, luis, centro, hoy, "09:00",
                "Malestar estomacal", EstadoCita.PENDIENTE, null);

        crearCita(pediatra, carmen, norte, hoy, "10:30",
                "Vacunacion infantil", EstadoCita.PENDIENTE, null);

        crearCita(ginecologo, luis, centro, hoy, "15:00",
                "Consulta de seguimiento", EstadoCita.PENDIENTE, null);

        crearCita(general, maria, centro, hoy.plusDays(1), "08:30",
                "Control de peso", EstadoCita.PENDIENTE, null);

        crearCita(general, jorge, centro, hoy.plusDays(1), "11:30",
                "Revision de resultados", EstadoCita.PENDIENTE, null);

        crearCita(pediatra, carmen, norte, hoy.plusDays(2), "09:00",
                "Control mensual del bebe", EstadoCita.PENDIENTE, null);

        crearCita(ginecologo, maria, centro, hoy.plusDays(3), "14:00",
                "Consulta preventiva", EstadoCita.PENDIENTE, null);

        crearCita(general, luis, centro, hoy.plusDays(4), "10:30",
                "Chequeo general", EstadoCita.PENDIENTE, null);

        crearCita(pediatra, jorge, norte, hoy.plusDays(5), "16:30",
                "Evaluacion de dermatitis", EstadoCita.PENDIENTE, null);

        crearCita(ginecologo, carmen, centro, hoy.plusDays(6), "09:30",
                "Revision de anticonceptivos", EstadoCita.PENDIENTE, null);
    }

    private Cita crearCita(Medico medico, Paciente paciente, Sede sede,
                           LocalDate fecha, String hora, String motivo,
                           EstadoCita estado, String diagnostico) {
        Cita cita = new Cita();
        cita.setMedico(medico);
        cita.setPaciente(paciente);
        cita.setSede(sede);
        cita.setFecha(fecha);
        cita.setHora(LocalTime.parse(hora));
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        cita.setDiagnostico(diagnostico);
        return citaRepositorio.save(cita);
    }

    private void guardarReceta(Cita cita, String indicaciones, String medicamentos) {
        Receta receta = new Receta();
        receta.setCita(cita);
        receta.setIndicaciones(indicaciones);
        receta.setMedicamentos(medicamentos);
        receta.setCreadoEn(LocalDateTime.now().minusDays(1));
        recetaRepositorio.save(receta);
    }

    private Usuario crearUsuario(String username, String password, String nombre, String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombreCompleto(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private Paciente crearPaciente(Usuario usuario, String dni, String telefono,
                                   int anio, int mes, int dia) {
        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setDni(dni);
        paciente.setTelefono(telefono);
        paciente.setFechaNacimiento(LocalDate.of(anio, mes, dia));
        return paciente;
    }

    private Sede crearSede(String nombre, String direccion, String telefono) {
        Sede sede = new Sede();
        sede.setNombre(nombre);
        sede.setDireccion(direccion);
        sede.setTelefono(telefono);
        sede.setActivo(true);
        return sede;
    }

    private Especialidad crearEspecialidad(String nombre, String descripcion) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(descripcion);
        return especialidad;
    }

    private Medico crearMedico(Usuario usuario, Especialidad especialidad, Sede sede, String cmp) {
        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidad);
        medico.setSede(sede);
        medico.setCmp(cmp);
        medico.setActivo(true);
        return medico;
    }
}
