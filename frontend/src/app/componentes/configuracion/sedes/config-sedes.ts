import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { Sede } from '../../../modelos/sede';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-config-sedes',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './config-sedes.html',
  styleUrls: ['./config-sedes.css', '../shared/config-shared.css']
})
export class ConfigSedesComponent implements OnInit {
  sedes: Sede[] = [];
  form: FormGroup;
  editandoId: number | null = null;
  modalOpen = false;
  mensaje = '';
  error = '';
  submitted = false;

  constructor(private fb: FormBuilder, private sedeService: SedeService) {
    this.form = this.crearFormulario();
  }

  private crearFormulario(): FormGroup {
    return this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
      direccion: ['', [Validators.maxLength(200)]],
      telefono: ['', [Validators.pattern(/^\d{0,20}$/)]],
      activo: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.sedeService.listarTodas().subscribe({
      next: d => this.sedes = d,
      error: e => this.error = e?.error?.message || 'Error al cargar sedes'
    });
  }

  abrirNuevo(): void {
    this.form = this.crearFormulario();
    this.editandoId = null;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  editar(s: Sede): void {
    this.form = this.fb.group({
      nombre: [(s.nombre || ''), [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
      direccion: [s.direccion || '', [Validators.maxLength(200)]],
      telefono: [s.telefono || '', [Validators.pattern(/^\d{0,20}$/)]],
      activo: [s.activo !== false]
    });
    this.editandoId = s.id;
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
      return nombre === 'nombre' ? 'Ingresa el nombre de la sede' : 'Este campo es obligatorio';
    }
    if (c.hasError('minlength')) return `Debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    if (c.hasError('pattern')) return 'Solo se permiten números';
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    if (this.mostrarError('nombre')) return 'Ingresa el nombre de la sede.';
    if (this.mostrarError('telefono')) return 'El teléfono debe ser solo numérico.';
    return '';
  }

  guardar(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const payload = {
      nombre: String(v['nombre']).trim(),
      direccion: String(v['direccion'] ?? '').trim(),
      telefono: String(v['telefono'] ?? '').trim(),
      activo: v['activo'] !== false
    };

    const req = this.editandoId
      ? this.sedeService.actualizar({ id: this.editandoId, ...payload } as Sede)
      : this.sedeService.crear(payload);
    req.subscribe({
      next: () => {
        this.mensaje = 'Sede guardada';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar sede'
    });
  }

  desactivar(s: Sede): void {
    this.sedeService.desactivar(s.id).subscribe({
      next: () => { this.mensaje = 'Sede desactivada'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al desactivar'
    });
  }

  activar(s: Sede): void {
    this.sedeService.activar(s).subscribe({
      next: () => { this.mensaje = 'Sede activada'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al activar'
    });
  }
}
