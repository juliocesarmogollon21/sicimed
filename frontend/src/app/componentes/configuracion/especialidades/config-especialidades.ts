import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { Especialidad } from '../../../modelos/especialidad';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-config-especialidades',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './config-especialidades.html',
  styleUrls: ['./config-especialidades.css', '../shared/config-shared.css']
})
export class ConfigEspecialidadesComponent implements OnInit {
  especialidades: Especialidad[] = [];
  form: FormGroup;
  editandoId: number | null = null;
  modalOpen = false;
  mensaje = '';
  error = '';
  submitted = false;

  constructor(private fb: FormBuilder, private espService: EspecialidadService) {
    this.form = this.crearFormulario();
  }

  private crearFormulario(): FormGroup {
    return this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      descripcion: ['', [Validators.maxLength(300)]]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.espService.listar().subscribe({
      next: d => this.especialidades = d,
      error: e => this.error = e?.error?.message || 'Error al cargar especialidades'
    });
  }

  abrirNuevo(): void {
    this.form = this.crearFormulario();
    this.editandoId = null;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  editar(e: Especialidad): void {
    this.form = this.fb.group({
      nombre: [e.nombre || '', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      descripcion: [e.descripcion || '', [Validators.maxLength(300)]]
    });
    this.editandoId = e.id ?? null;
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
    if (c.hasError('required')) return 'Ingresa el nombre de la especialidad';
    if (c.hasError('minlength')) return `Debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    return this.mostrarError('nombre')
      ? 'Ingresa el nombre de la especialidad (mínimo 3 caracteres).'
      : '';
  }

  guardar(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const payload: Partial<Especialidad> = {
      nombre: String(v['nombre']).trim(),
      descripcion: String(v['descripcion'] ?? '').trim()
    };

    const req = this.editandoId
      ? this.espService.actualizar({ id: this.editandoId, ...payload } as Especialidad)
      : this.espService.crear(payload);
    req.subscribe({
      next: () => {
        this.mensaje = 'Especialidad guardada';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar'
    });
  }
}
