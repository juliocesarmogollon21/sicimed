import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsuarioService, UsuarioDto } from '../../../servicios/usuario.service';

@Component({
  selector: 'app-config-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './config-usuarios.html',
  styleUrls: ['./config-usuarios.css', '../shared/config-shared.css']
})
export class ConfigUsuariosComponent implements OnInit {
  usuarios: UsuarioDto[] = [];
  form: UsuarioDto = { username: '', password: '', nombreCompleto: '', email: '', rol: 'RECEPCIONISTA', activo: true };
  modalOpen = false;
  mensaje = '';
  error = '';

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.usuarioService.listar().subscribe({
      next: d => this.usuarios = d,
      error: e => this.error = e?.error?.message || 'Error al cargar usuarios'
    });
  }

  abrirNuevo(): void {
    this.form = { username: '', password: '', nombreCompleto: '', email: '', rol: 'RECEPCIONISTA', activo: true };
    this.error = '';
    this.modalOpen = true;
  }

  editar(u: UsuarioDto): void {
    this.form = { ...u, password: '' };
    this.error = '';
    this.modalOpen = true;
  }

  cerrar(): void { this.modalOpen = false; }

  guardar(): void {
    if (!this.form.username?.trim() || !this.form.nombreCompleto?.trim()) {
      this.error = 'Username y nombre son obligatorios';
      return;
    }
    if (!this.form.id && !this.form.password) {
      this.error = 'Password obligatorio al crear';
      return;
    }
    const req = this.form.id
      ? this.usuarioService.actualizar(this.form)
      : this.usuarioService.crear(this.form);
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
      next: () => { this.mensaje = 'Usuario desactivado'; this.cargar(); }
    });
  }

  activar(u: UsuarioDto): void {
    this.usuarioService.activar(u).subscribe({
      next: () => { this.mensaje = 'Usuario activado'; this.cargar(); }
    });
  }
}
