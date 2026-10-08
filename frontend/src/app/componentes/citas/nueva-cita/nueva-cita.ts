import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { MedicoService } from '../../../servicios/medico.service';
import { CitaService } from '../../../servicios/cita.service';
import { PacienteService } from '../../../servicios/paciente.service';
import { AuthService } from '../../../servicios/auth.service';
import { Sede } from '../../../modelos/sede';
import { Especialidad } from '../../../modelos/especialidad';
import { Medico } from '../../../modelos/medico';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-nueva-cita',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './nueva-cita.html',
  styleUrl: './nueva-cita.css'
})
export class NuevaCitaComponent implements OnInit {
  sedes: Sede[] = [];
  especialidades: Especialidad[] = [];
  medicos: Medico[] = [];
  horas: string[] = [];

  pacienteId: number | null = null;
  pacienteNombre = '';

  form!: FormGroup;
  fechaMinima = this.hoyIso();
  error = '';
  ok = '';
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private sedeService: SedeService,
    private especialidadService: EspecialidadService,
    private medicoService: MedicoService,
    private citaService: CitaService,
    private pacienteService: PacienteService,
    public auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      // Solo aplica al rol recepción: el campo no se renderiza para pacientes,
      // por eso solo se valida el formato y la obligatoriedad se revisa en buscarPaciente().
      dniBusqueda: ['', [Validators.pattern(/^\d{1,20}$/)]],
      sedeId: [null, [Validators.required]],
      especialidadId: [null, [Validators.required]],
      medicoId: [null, [Validators.required]],
      fecha: ['', [Validators.required, this.fechaNoPasadaValidator()]],
      hora: ['', [Validators.required]],
      motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.sedeService.listar().subscribe(d => this.sedes = d);
    this.especialidadService.listar().subscribe(d => this.especialidades = d);
    const session = this.auth.session();
    if (session?.pacienteId) {
      this.pacienteId = session.pacienteId;
      this.pacienteNombre = session.nombreCompleto;
    }
  }

  get esRecepcion(): boolean {
    return this.auth.hasRole('RECEPCIONISTA', 'ADMIN');
  }

  // ---------- Validación en vivo ----------

  /** No permite agendar en una fecha anterior a hoy. */
  private fechaNoPasadaValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const valor = control.value;
      if (!valor) return null; // el required se encarga
      return this.esFechaPasada(valor) ? { fechaPasada: true } : null;
    };
  }

  mostrarError(nombre: string): boolean {
    const c = this.form.get(nombre);
    if (!c) return false;
    if (nombre === 'dniBusqueda' && !this.esRecepcion) return false;
    return c.invalid && (c.touched || this.submitted);
  }

  mensajeError(nombre: string): string {
    const c = this.form.get(nombre)!;
    if (c.hasError('required')) {
      switch (nombre) {
        case 'dniBusqueda': return 'Ingresa el DNI del paciente (solo números)';
        case 'sedeId': return 'Selecciona una sede';
        case 'especialidadId': return 'Selecciona una especialidad';
        case 'medicoId': return 'Selecciona un médico';
        case 'fecha': return 'Selecciona la fecha de la cita';
        case 'hora': return 'Selecciona una hora disponible';
        case 'motivo': return 'Describe el motivo de la consulta';
        default: return 'Este campo es obligatorio';
      }
    }
    if (c.hasError('fechaPasada')) return 'No puedes agendar en una fecha pasada. Elige hoy o una fecha posterior.';
    if (c.hasError('pattern')) return 'El DNI debe ser solo numérico';
    if (c.hasError('minlength')) {
      return `El motivo debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    }
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    const pendientes: string[] = [];
    if (this.esRecepcion && this.mostrarError('dniBusqueda')) pendientes.push('DNI del paciente');
    if (this.mostrarError('sedeId')) pendientes.push('sede');
    if (this.mostrarError('especialidadId')) pendientes.push('especialidad');
    if (this.mostrarError('medicoId')) pendientes.push('médico');
    if (this.mostrarError('fecha')) {
      pendientes.push(this.form.get('fecha')?.hasError('fechaPasada') ? 'una fecha de hoy en adelante' : 'fecha');
    }
    if (this.mostrarError('hora')) pendientes.push('hora');
    if (this.mostrarError('motivo')) pendientes.push('motivo (mínimo 10 caracteres)');
    if (!pendientes.length) return '';
    return 'Completa: ' + pendientes.join(', ') + '.';
  }

  // ---------- Lógica de negocio ----------

  buscarPaciente(): void {
    this.error = '';
    const dni = String(this.form.get('dniBusqueda')?.value ?? '').trim();
    this.form.get('dniBusqueda')?.markAsTouched();
    if (!dni) {
      this.error = 'Ingrese DNI';
      return;
    }
    this.pacienteService.buscarPorDni(dni).subscribe({
      next: p => {
        this.pacienteId = p.id;
        this.pacienteNombre = p.nombreCompleto;
        this.ok = `Paciente: ${p.nombreCompleto}`;
      },
      error: err => {
        this.pacienteId = null;
        this.pacienteNombre = '';
        this.error = err?.error?.message || 'Paciente no encontrado';
      }
    });
  }

  onFiltroChange(): void {
    const sedeId = this.form.get('sedeId')!.value as number | null;
    const especialidadId = this.form.get('especialidadId')!.value as number | null;
    this.form.get('medicoId')!.setValue(null);
    this.form.get('hora')!.setValue('');
    this.horas = [];
    this.medicoService.listar(sedeId, especialidadId).subscribe(d => this.medicos = d);
  }

  private hoyIso(): string {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }

  private esFechaPasada(fecha: string): boolean {
    if (!fecha) return true;
    return fecha < this.hoyIso();
  }

  onFechaMedicoChange(): void {
    const medicoId = this.form.get('medicoId')!.value as number | null;
    const fecha = String(this.form.get('fecha')?.value ?? '');
    this.horas = [];
    this.form.get('hora')!.setValue('');
    if (medicoId && fecha) {
      this.citaService.disponibilidad(medicoId, fecha).subscribe({
        next: h => this.horas = h,
        error: err => this.error = err?.error?.message || 'No se pudo cargar disponibilidad'
      });
    }
  }

  guardar(): void {
    this.submitted = true;
    this.error = '';
    this.ok = '';

    if (this.esRecepcion) this.form.get('dniBusqueda')?.markAsTouched();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.pacienteId) {
      this.error = this.esRecepcion ? 'Busque al paciente por DNI' : 'Sesión de paciente requerida';
      return;
    }

    const v = this.form.getRawValue();
    const fecha = String(v['fecha']);
    
    if (this.esFechaPasada(fecha)) {
      this.form.get('fecha')!.setValue(fecha);
      this.error = 'No puedes agendar en una fecha pasada. Elige hoy o una fecha posterior.';
      return;
    }

    let hora = String(v['hora']);
    // Normalizar a HH:mm (el backend espera este formato)
    if (hora.length > 5) {
      hora = hora.substring(0, 5);
    }

    this.citaService.crear({
      medicoId: v['medicoId'] as number,
      pacienteId: this.pacienteId,
      sedeId: v['sedeId'] as number,
      fecha: fecha,
      hora: hora,
      motivo: String(v['motivo'] ?? '')
    }).subscribe({
      next: () => {
        this.ok = 'Cita registrada';
        setTimeout(() => this.router.navigate(['/citas']), 800);
      },
      error: err => {
        if (err.status === 409) {
          this.error = err?.error?.message || 'Ese horario ya está ocupado. Elige otra fecha u hora.';
        } else {
          this.error = err?.error?.message || 'No se pudo registrar la cita. Revisa los datos.';
        }
      }
    });
  }
}
