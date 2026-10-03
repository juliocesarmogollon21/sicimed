import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../servicios/auth.service';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-registro',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  form: FormGroup;
  error = '';
  cargando = false;
  submitted = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      dni: ['', [Validators.required, Validators.pattern(/^\d{1,20}$/)]],
      telefono: ['', [Validators.pattern(/^\d{0,20}$/)]]
    });
  }

  mostrarError(nombre: string): boolean {
    const c = this.form.get(nombre)!;
    return c.invalid && (c.touched || this.submitted);
  }

  mensajeError(nombre: string): string {
    const c = this.form.get(nombre)!;
    if (c.hasError('required')) {
      switch (nombre) {
        case 'nombreCompleto': return 'Ingresa tu nombre completo';
        case 'username': return 'Ingresa un nombre de usuario';
        case 'password': return 'Ingresa una contraseña';
        case 'email': return 'Ingresa tu correo electrónico';
        case 'dni': return 'Ingresa tu DNI';
        default: return 'Este campo es obligatorio';
      }
    }
    if (c.hasError('minlength')) {
      const min = c.getError('minlength')?.requiredLength;
      return `Debe tener al menos ${min} caracteres`;
    }
    if (c.hasError('email')) return 'Ingresa un correo electrónico válido';
    if (c.hasError('pattern')) {
      return nombre === 'dni' ? 'El DNI debe ser solo numérico' : 'Solo se permiten números';
    }
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    const pendientes: string[] = [];
    if (this.mostrarError('nombreCompleto')) pendientes.push('nombre completo');
    if (this.mostrarError('username')) pendientes.push('usuario');
    if (this.mostrarError('password')) pendientes.push('contraseña (mínimo 6 caracteres)');
    if (this.mostrarError('email')) pendientes.push('correo electrónico válido');
    if (this.mostrarError('dni')) pendientes.push('DNI numérico');
    if (!pendientes.length) return '';
    return 'Completa: ' + pendientes.join(', ') + '.';
  }

  registrar(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    const raw = this.form.getRawValue();
    this.auth.register({
      username: String(raw['username']).trim(),
      password: String(raw['password']),
      nombreCompleto: String(raw['nombreCompleto']).trim(),
      email: String(raw['email']).trim(),
      dni: String(raw['dni']).trim(),
      telefono: String(raw['telefono']).trim()
    }).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.cargando = false;
        this.error = err?.error?.message || 'No se pudo crear la cuenta';
      }
    });
  }
}
