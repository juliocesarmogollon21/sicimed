import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../servicios/auth.service';
import { AlertaComponent } from '../../compartido/alerta/alerta';

@Component({
  selector: 'app-login',
  imports: [AlertaComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  form: FormGroup;
  error = '';
  cargando = false;
  submitted = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  get username() { return this.form.get('username')!; }
  get password() { return this.form.get('password')!; }

  /** El error se muestra al salir del campo (blur) o tras intentar enviar. */
  mostrarError(controlName: 'username' | 'password'): boolean {
    const c = this.form.get(controlName)!;
    return c.invalid && (c.touched || this.submitted);
  }

  mensajeError(controlName: 'username' | 'password'): string {
    const c = this.form.get(controlName)!;
    if (c.hasError('required')) {
      return controlName === 'username' ? 'Ingresa tu usuario' : 'Ingresa tu contraseña';
    }
    if (c.hasError('maxlength')) return 'Valor demasiado largo';
    return 'Revisa este campo';
  }

  pistaBloqueo(): string {
    if (this.mostrarError('username') && this.mostrarError('password')) {
      return 'Ingresa tu usuario y tu contraseña para continuar.';
    }
    if (this.mostrarError('username')) return 'Ingresa tu usuario para continuar.';
    if (this.mostrarError('password')) return 'Ingresa tu contraseña para continuar.';
    return '';
  }

  ingresar(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    const raw = this.form.getRawValue();
    this.auth.login(String(raw['username']).trim(), String(raw['password'])).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.cargando = false;
        this.error = err?.error?.message || 'Credenciales inválidas';
      }
    });
  }
}
