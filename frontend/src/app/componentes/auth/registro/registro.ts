import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../servicios/auth.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  username = '';
  password = '';
  nombreCompleto = '';
  email = '';
  dni = '';
  telefono = '';
  error = '';
  cargando = false;

  constructor(private auth: AuthService, private router: Router) {}

  registrar(): void {
    this.error = '';
    this.cargando = true;
    this.auth.register({
      username: this.username.trim(),
      password: this.password,
      nombreCompleto: this.nombreCompleto.trim(),
      email: this.email.trim(),
      dni: this.dni.trim(),
      telefono: this.telefono.trim()
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
