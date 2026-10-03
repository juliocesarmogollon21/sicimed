import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicoService } from '../../../servicios/medico.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { SedeService } from '../../../servicios/sede.service';
import { UsuarioService, UsuarioDto } from '../../../servicios/usuario.service';
import { Medico } from '../../../modelos/medico';
import { Especialidad } from '../../../modelos/especialidad';
import { Sede } from '../../../modelos/sede';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-config-medicos',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './config-medicos.html',
  styleUrls: ['./config-medicos.css', '../shared/config-shared.css']
})
export class ConfigMedicosComponent implements OnInit {
  medicos: Medico[] = [];
  especialidades: Especialidad[] = [];
  sedes: Sede[] = [];
  usuariosMedico: UsuarioDto[] = [];
  form: FormGroup;
  editandoId: number | null = null;
  modalOpen = false;
  mensaje = '';
  error = '';
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private medicoService: MedicoService,
    private espService: EspecialidadService,
    private sedeService: SedeService,
    private usuarioService: UsuarioService
  ) {
    this.form = this.crearFormulario();
  }

  private crearFormulario(): FormGroup {
    return this.fb.group({
      usuarioId: [null, [Validators.required]],
      especialidadId: [null, [Validators.required]],
      sedeId: [null, [Validators.required]],
      cmp: ['', [Validators.pattern(/^[A-Za-z0-9\s-]{0,20}$/)]],
      activo: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.medicoService.listarTodos().subscribe({
      next: d => this.medicos = d,
      error: () => this.medicoService.listar().subscribe(d => this.medicos = d)
    });
    this.espService.listar().subscribe({ next: d => this.especialidades = d });
    this.sedeService.listarTodas().subscribe({ next: d => this.sedes = d });
    this.usuarioService.listar().subscribe({
      next: d => this.usuariosMedico = d.filter(u => (u.rol || '').toUpperCase() === 'MEDICO'),
      error: e => this.error = e?.error?.message || 'Error usuarios'
    });
  }

  abrirNuevo(): void {
    this.form = this.crearFormulario();
    this.editandoId = null;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  editar(m: Medico): void {
    this.form = this.fb.group({
      usuarioId: [m.usuarioId, [Validators.required]],
      especialidadId: [m.especialidadId, [Validators.required]],
      sedeId: [m.sedeId, [Validators.required]],
      cmp: [m.cmp || '', [Validators.pattern(/^[A-Za-z0-9\s-]{0,20}$/)]],
      activo: [m.activo !== false]
    });
    this.editandoId = m.id;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  cerrar(): void { this.modalOpen = false; }

  mostrarError(nombre: string): boolean {
    const c = this.form.get(nombre);
    return !!c && c.invalid && (c.touched || this.submitted);
  }

  mensajeError(nombre: string): string {
    const c = this.form.get(nombre)!;
    if (c.hasError('required')) {
      switch (nombre) {
        case 'usuarioId': return 'Selecciona el usuario con rol MÉDICO';
        case 'especialidadId': return 'Selecciona una especialidad';
        case 'sedeId': return 'Selecciona una sede';
        default: return 'Este campo es obligatorio';
      }
    }
    if (c.hasError('pattern')) return 'El CMP solo admite letras, números, espacios y guiones';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    const pendientes: string[] = [];
    if (this.mostrarError('usuarioId')) pendientes.push('usuario');
    if (this.mostrarError('especialidadId')) pendientes.push('especialidad');
    if (this.mostrarError('sedeId')) pendientes.push('sede');
    if (this.mostrarError('cmp')) pendientes.push('formato del CMP');
    if (!pendientes.length) return '';
    return 'Completa: ' + pendientes.join(', ') + '.';
  }

  guardar(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const body = {
      usuarioId: v['usuarioId'] as number,
      especialidadId: v['especialidadId'] as number,
      sedeId: v['sedeId'] as number,
      cmp: String(v['cmp'] ?? '').trim() || undefined,
      activo: v['activo'] !== false
    };
    const req = this.editandoId
      ? this.medicoService.actualizar({
          id: this.editandoId,
          ...body,
          cmp: body.cmp,
          nombreCompleto: '',
          especialidadNombre: '',
          sedeNombre: ''
        })
      : this.medicoService.crear(body);
    req.subscribe({
      next: () => {
        this.mensaje = 'Médico guardado';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar médico'
    });
  }

  desactivar(m: Medico): void {
    this.medicoService.desactivar(m.id).subscribe({
      next: () => { this.mensaje = 'Médico desactivado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al desactivar'
    });
  }

  activar(m: Medico): void {
    this.medicoService.activar(m).subscribe({
      next: () => { this.mensaje = 'Médico activado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al activar'
    });
  }
}
