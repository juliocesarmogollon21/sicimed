import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { MedicoService } from '../../../servicios/medico.service';
import { CitaService } from '../../../servicios/cita.service';
import { PacienteService } from '../../../servicios/paciente.service';
import { AuthService } from '../../../servicios/auth.service';
import { Sede } from '../../../modelos/sede';
import { Especialidad } from '../../../modelos/especialidad';
import { Medico } from '../../../modelos/medico';
import { Cita } from '../../../modelos/cita';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-reprogramar-cita',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './reprogramar-cita.html',
  styleUrl: './reprogramar-cita.css'
})
export class ReprogramarCitaComponent implements OnInit {
  citaId: number | null = null;
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
  cargando = true;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private sedeService: SedeService,
    private especialidadService: EspecialidadService,
    private medicoService: MedicoService,
    private citaService: CitaService,
    private pacienteService: PacienteService,
    public auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      // Opcional: si se deja vacío se conserva el paciente actual de la cita.
      dniBusqueda: ['', [Validators.pattern(/^\d{1,20}$/)]],
      sedeId: [null, [Validators.required]],
      especialidadId: [null],
      medicoId: [null, [Validators.required]],
      fecha: ['', [Validators.required, this.fechaNoPasadaValidator()]],
      hora: ['', [Validators.required]],
      motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
  }

  get esRecepcion(): boolean {
    return this.auth.hasRole('RECEPCIONISTA', 'ADMIN');
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error = 'Cita no válida';
      this.cargando = false;
      return;
    }
    this.citaId = id;
    this.sedeService.listar().subscribe(d => this.sedes = d);
    this.especialidadService.listar().subscribe(d => this.especialidades = d);
    this.citaService.obtener(id).subscribe({
      next: (c: Cita) => {
        if (c.estado !== 'PENDIENTE') {
          this.error = 'Solo se pueden reprogramar citas pendientes';
          this.cargando = false;
          return;
        }
        this.pacienteId = c.pacienteId;
        this.pacienteNombre = c.pacienteNombre;
        this.form.patchValue({
          sedeId: c.sedeId,
          medicoId: c.medicoId,
          fecha: c.fecha,
          hora: (c.hora || '').substring(0, 5),
          motivo: c.motivo || ''
        });
        const sedeId = c.sedeId as number | null;
        this.medicoService.listar(sedeId, null).subscribe(meds => {
          this.medicos = meds;
          const m = meds.find(x => x.id === c.medicoId);
          if (m?.especialidadId) this.form.get('especialidadId')!.setValue(m.especialidadId);
          this.cargarHoras(true);
          this.cargando = false;
        });
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'No se pudo cargar la cita';
        this.cargando = false;
      }
    });
  }

  // ---------- Validación en vivo ----------

  private fechaNoPasadaValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const valor = control.value;
      if (!valor) return null;
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
        case 'dniBusqueda': return 'Ingresa el DNI del paciente';
        case 'sedeId': return 'Selecciona una sede';
        case 'medicoId': return 'Selecciona un médico';
        case 'fecha': return 'Selecciona la nueva fecha';
        case 'hora': return 'Selecciona una hora disponible';
        case 'motivo': return 'Describe el motivo de la consulta';
        default: return 'Este campo es obligatorio';
      }
    }
    if (c.hasError('fechaPasada')) return 'No puedes reprogramar a una fecha pasada. Elige hoy o una fecha posterior.';
    if (c.hasError('pattern')) return 'El DNI debe ser solo numérico';
    if (c.hasError('minlength')) return `El motivo debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    const pendientes: string[] = [];
    if (this.esRecepcion && this.mostrarError('dniBusqueda')) pendientes.push('DNI del paciente');
    if (this.mostrarError('sedeId')) pendientes.push('sede');
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
    if (!this.esRecepcion) return;
    this.error = '';
    this.form.get('dniBusqueda')?.markAsTouched();
    const dni = String(this.form.get('dniBusqueda')?.value ?? '').trim();
    if (!dni) {
      this.error = 'Ingrese DNI';
      return;
    }
    this.pacienteService.buscarPorDni(dni).subscribe({
      next: p => {
        this.pacienteId = p.id;
        this.pacienteNombre = p.nombreCompleto;
      },
      error: (err: any) => this.error = err?.error?.message || 'Paciente no encontrado'
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
    this.cargarHoras(false);
  }

  private cargarHoras(keepHora: boolean): void {
    const medicoId = this.form.get('medicoId')!.value as number | null;
    const fecha = String(this.form.get('fecha')?.value ?? '');
    const prev = keepHora ? String(this.form.get('hora')?.value ?? '') : '';
    this.horas = [];
    if (!keepHora) this.form.get('hora')!.setValue('');
    if (medicoId && fecha && this.citaId) {
      this.citaService.disponibilidad(medicoId, fecha, this.citaId).subscribe({
        next: h => {
          this.horas = h.map(x => x.length >= 5 ? x.substring(0, 5) : x);
          if (keepHora && prev && !this.horas.includes(prev)) {
            this.horas = [prev, ...this.horas];
          }
          if (keepHora) this.form.get('hora')!.setValue(prev);
        },
        error: (err: any) => this.error = err?.error?.message || 'No se pudo cargar disponibilidad'
      });
    }
  }

  guardar(): void {
    this.submitted = true;
    this.error = '';
    this.ok = '';

    if (this.esRecepcion) this.form.get('dniBusqueda')?.markAsTouched();

    const fechaActual = String(this.form.get('fecha')?.value ?? '');
    if (fechaActual && this.esFechaPasada(fechaActual)) {
      this.form.get('fecha')!.markAsTouched();
      this.form.get('fecha')!.updateValueAndValidity();
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.citaId || !this.pacienteId) {
      this.error = 'No se pudo determinar el paciente de la cita';
      return;
    }

    const v = this.form.getRawValue();
    let hora = String(v['hora']);
    // Normalizar a HH:mm (el backend espera este formato)
    if (hora.length > 5) {
      hora = hora.substring(0, 5);
    }
    this.citaService.actualizar(this.citaId, {
      medicoId: v['medicoId'] as number,
      pacienteId: this.pacienteId,
      sedeId: v['sedeId'] as number,
      fecha: String(v['fecha']),
      hora: hora,
      motivo: String(v['motivo'] ?? '')
    }).subscribe({
      next: () => {
        this.ok = 'Cita reprogramada';
        setTimeout(() => this.router.navigate(['/citas']), 700);
      },
      error: (err: any) => {
        if (err.status === 409) {
          this.error = err?.error?.message || 'Horario ocupado (conflicto 409)';
        } else {
          this.error = err?.error?.message || 'Ese horario ya está ocupado o no se pudo reprogramar.';
        }
      }
    });
  }
}
