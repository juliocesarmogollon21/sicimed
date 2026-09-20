import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { Sede } from '../../../modelos/sede';

@Component({
  selector: 'app-config-sedes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './config-sedes.html',
  styleUrls: ['./config-sedes.css', '../shared/config-shared.css']
})
export class ConfigSedesComponent implements OnInit {
  sedes: Sede[] = [];
  form: Partial<Sede> = { nombre: '', direccion: '', telefono: '', activo: true };
  modalOpen = false;
  mensaje = '';
  error = '';

  constructor(private sedeService: SedeService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.sedeService.listarTodas().subscribe({
      next: d => this.sedes = d,
      error: e => this.error = e?.error?.message || 'Error al cargar sedes'
    });
  }

  abrirNuevo(): void {
    this.form = { nombre: '', direccion: '', telefono: '', activo: true };
    this.error = '';
    this.modalOpen = true;
  }

  editar(s: Sede): void {
    this.form = { ...s };
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
      ? this.sedeService.actualizar(this.form as Sede)
      : this.sedeService.crear(this.form);
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
