import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsuarioService, UsuarioDto } from '../../../servicios/usuario.service';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-config-usuarios',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './config-usuarios.html',
  styleUrls: ['./config-usuarios.css', '../shared/config-shared.css']
})
export class ConfigUsuariosComponent implements OnInit {
  usuarios: UsuarioDto[] = [];
  form: FormGroup;
  editandoId: number | null = null;
  modalOpen = false;
  mensaje = '';
  error = '';
  submitted = false;

  readonly roles = ['ADMIN', 'RECEPCIONISTA', 'MEDICO', 'PACIENTE'];

  constructor(private fb: FormBuilder, private usuarioService: UsuarioService) {
    this.form = this.crearFormulario(false);
  }

  /**
   * Al crear, la contraseña es obligatoria (mínimo 6 caracteres).
   * Al editar es opcional: si se deja vacía no se modifica.
   */
  private crearFormulario(esEdicion: boolean): FormGroup {
    const password = esEdicion
      ? ['', [Validators.minLength(6), Validators.maxLength(100)]]
      : ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]];
    return this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password,
      nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.email, Validators.maxLength(120)]],
      rol: ['RECEPCIONISTA', [Validators.required]],
      activo: [true]
    });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.usuarioService.listar().subscribe({
      next: d => this.usuarios = d,
      error: e => this.error = e?.error?.message || 'Error al cargar usuarios'
    });
  }

  abrirNuevo(): void {
    this.form = this.crearFormulario(false);
    this.editandoId = null;
    this.error = '';
    this.submitted = false;
    this.modalOpen = true;
  }

  editar(u: UsuarioDto): void {
    this.form = this.crearFormulario(true);
    this.form.patchValue({
      username: u.username || '',
      nombreCompleto: u.nombreCompleto || '',
      email: u.email || '',
      rol: u.rol || 'RECEPCIONISTA',
      activo: u.activo !== false,
      password: ''
    });
    this.editandoId = u.id ?? null;
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
        case 'username': return 'Ingresa un nombre de usuario';
        case 'password': return 'Ingresa una contraseña';
        case 'nombreCompleto': return 'Ingresa el nombre completo';
        case 'rol': return 'Selecciona un rol';
        default: return 'Este campo es obligatorio';
      }
    }
    if (c.hasError('minlength')) return `Debe tener al menos ${c.getError('minlength')?.requiredLength} caracteres`;
    if (c.hasError('email')) return 'Ingresa un correo electrónico válido';
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    const pendientes: string[] = [];
    if (this.mostrarError('username')) pendientes.push('usuario');
    if (this.mostrarError('nombreCompleto')) pendientes.push('nombre completo');
    if (this.mostrarError('password')) pendientes.push('contraseña (mínimo 6 caracteres)');
    if (this.mostrarError('email')) pendientes.push('correo electrónico válido');
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
    const payload: UsuarioDto = {
      username: String(v['username']).trim(),
      nombreCompleto: String(v['nombreCompleto']).trim(),
      email: String(v['email'] ?? '').trim(),
      rol: String(v['rol']),
      activo: v['activo'] !== false
    };
    const password = String(v['password'] ?? '');
    if (password) payload.password = password;

    const req = this.editandoId
      ? this.usuarioService.actualizar({ ...payload, id: this.editandoId })
      : this.usuarioService.crear(payload);
    req.subscribe({
      next: () => {
        this.mensaje = 'Usuario guardado';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar usuario'
    });
  }

  desactivar(u: UsuarioDto): void {
    if (u.id == null) return;
    this.usuarioService.desactivar(u.id).subscribe({
      next: () => { this.mensaje = 'Usuario desactivado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al desactivar'
    });
  }

  activar(u: UsuarioDto): void {
    this.usuarioService.activar(u).subscribe({
      next: () => { this.mensaje = 'Usuario activado'; this.cargar(); },
      error: e => this.error = e?.error?.message || 'Error al activar'
    });
  }
}
