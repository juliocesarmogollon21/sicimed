package com.sicimed.configuracion;

import com.sicimed.modelo.Especialidad;
import com.sicimed.modelo.Medico;
import com.sicimed.modelo.Medicamento;
import com.sicimed.modelo.Paciente;
import com.sicimed.modelo.Rol;
import com.sicimed.modelo.Sede;
import com.sicimed.modelo.Usuario;
import com.sicimed.repositorio.EspecialidadRepositorio;
import com.sicimed.repositorio.MedicoRepositorio;
import com.sicimed.repositorio.MedicamentoRepositorio;
import com.sicimed.repositorio.PacienteRepositorio;
import com.sicimed.repositorio.SedeRepositorio;
import com.sicimed.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final EstadoSemilla estadoSemilla;

    public DatosSemilla(UsuarioRepositorio usuarioRepositorio,
                        SedeRepositorio sedeRepositorio,
                        EspecialidadRepositorio especialidadRepositorio,
                        MedicoRepositorio medicoRepositorio,
                        PacienteRepositorio pacienteRepositorio,
                        MedicamentoRepositorio medicamentoRepositorio,
                        PasswordEncoder passwordEncoder,
                        EstadoSemilla estadoSemilla) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.sedeRepositorio = sedeRepositorio;
        this.especialidadRepositorio = especialidadRepositorio;
        this.medicoRepositorio = medicoRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.medicamentoRepositorio = medicamentoRepositorio;
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

        Usuario paciente1 = crearUsuario("paciente1", "paciente123", "Maria Perez", "paciente1@sicimed.pe", Rol.PACIENTE);
        usuarioRepositorio.save(paciente1);

        medicoRepositorio.save(crearMedico(medico1, general, sedeCentro, "CMP-12345"));
        medicoRepositorio.save(crearMedico(medico2, pediatria, sedeNorte, "CMP-67890"));
        medicoRepositorio.save(crearMedico(medico3, ginecologia, sedeCentro, "CMP-11223"));

        Paciente paciente = new Paciente();
        paciente.setUsuario(paciente1);
        paciente.setDni("71234567");
        paciente.setTelefono("999888777");
        paciente.setFechaNacimiento(java.time.LocalDate.of(1995, 5, 20));
        pacienteRepositorio.save(paciente);

        for (String nombre : List.of("Paracetamol 500mg", "Amoxicilina 500mg", "Ibuprofeno 400mg",
                "Loratadina 10mg", "Omeprazol 20mg", "Azitromicina 500mg")) {
            Medicamento medicamento = new Medicamento();
            medicamento.setNombre(nombre);
            medicamento.setActivo(true);
            medicamentoRepositorio.save(medicamento);
        }

        log.info("Datos semilla cargados. Usuarios: admin/admin123, recepcion/recep123, "
                + "medico1/medico123, paciente1/paciente123");
        estadoSemilla.marcarListo();
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
