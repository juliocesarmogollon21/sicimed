import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicamentoService } from '../../../servicios/medicamento.service';
import { Medicamento } from '../../../modelos/medicamento';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-config-medicamentos',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './config-medicamentos.html',
  styleUrls: ['./config-medicamentos.css', '../shared/config-shared.css']
})
export class ConfigMedicamentosComponent implements OnInit {
  medicamentos: Medicamento[] = [];
  form: FormGroup;
  editandoId: number | null = null;
  modalOpen = false;
  mensaje = '';
  error = '';
  submitted = false;

  constructor(private fb: FormBuilder, private medicamentoService: MedicamentoService) {
    this.form = this.crearFormulario();
  }

  private crearFormulario(): FormGroup {
    return this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      descripcion: ['', [Validators.maxLength(300)]],
      activo: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.medicamentoService.listar(true).subscribe({
      next: d => this.medicamentos = d,
      error: e => this.error = e?.error?.message || 'Error al cargar medicamentos'
    });
  }

  abrirNuevo(): void {
    this.form = this.crearFormulario();
    this.editandoId = null;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  editar(m: Medicamento): void {
    this.form = this.fb.group({
      nombre: [m.nombre || '', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      descripcion: [m.descripcion || '', [Validators.maxLength(300)]],
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
    if (c.hasError('required')) return 'Ingresa el nombre del medicamento';
    if (c.hasError('minlength')) return `Debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    return this.mostrarError('nombre')
      ? 'Ingresa el nombre del medicamento (mínimo 3 caracteres).'
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
    const body = {
      nombre: String(v['nombre']).trim(),
      descripcion: String(v['descripcion'] ?? '').trim(),
      activo: v['activo'] !== false
    };
    const req = this.editandoId
      ? this.medicamentoService.actualizar({ id: this.editandoId, ...body })
      : this.medicamentoService.crear(body);
    req.subscribe({
      next: () => {
        this.mensaje = 'Medicamento guardado';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar medicamento'
    });
  }

  desactivar(m: Medicamento): void {
    this.medicamentoService.desactivar(m.id).subscribe({
      next: () => { this.mensaje = 'Medicamento desactivado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al desactivar'
    });
  }

  activar(m: Medicamento): void {
    this.medicamentoService.activar(m).subscribe({
      next: () => { this.mensaje = 'Medicamento activado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al activar'
    });
  }
}
