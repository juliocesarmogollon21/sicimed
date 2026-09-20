import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { Especialidad } from '../../../modelos/especialidad';

@Component({
  selector: 'app-config-especialidades',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './config-especialidades.html',
  styleUrls: ['./config-especialidades.css', '../shared/config-shared.css']
})
export class ConfigEspecialidadesComponent implements OnInit {
  especialidades: Especialidad[] = [];
  form: Partial<Especialidad> = { nombre: '', descripcion: '' };
  modalOpen = false;
  mensaje = '';
  error = '';

  constructor(private espService: EspecialidadService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.espService.listar().subscribe({
      next: d => this.especialidades = d,
      error: e => this.error = e?.error?.message || 'Error al cargar especialidades'
    });
  }

  abrirNuevo(): void {
    this.form = { nombre: '', descripcion: '' };
    this.error = '';
    this.modalOpen = true;
  }

  editar(e: Especialidad): void {
    this.form = { ...e };
    this.error = '';
    this.modalOpen = true;
  }

  cerrar(): void { this.modalOpen = false; }

  guardar(): void {
    if (!this.form.nombre?.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    const req = this.form.id
      ? this.espService.actualizar(this.form as Especialidad)
      : this.espService.crear(this.form);
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
