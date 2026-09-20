import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicamentoService } from '../../../servicios/medicamento.service';
import { Medicamento } from '../../../modelos/medicamento';

@Component({
  selector: 'app-config-medicamentos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './config-medicamentos.html',
  styleUrls: ['./config-medicamentos.css', '../shared/config-shared.css']
})
export class ConfigMedicamentosComponent implements OnInit {
  medicamentos: Medicamento[] = [];
  form: Partial<Medicamento> = { nombre: '', descripcion: '', activo: true };
  modalOpen = false;
  mensaje = '';
  error = '';

  constructor(private medicamentoService: MedicamentoService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.medicamentoService.listar(true).subscribe({
      next: d => this.medicamentos = d,
      error: e => this.error = e?.error?.message || 'Error al cargar medicamentos'
    });
  }

  abrirNuevo(): void {
    this.form = { nombre: '', descripcion: '', activo: true };
    this.error = '';
    this.modalOpen = true;
  }

  editar(m: Medicamento): void {
    this.form = { ...m };
    this.error = '';
    this.modalOpen = true;
  }

  cerrar(): void { this.modalOpen = false; }

  guardar(): void {
    if (!this.form.nombre?.trim()) {
      this.error = 'Nombre del medicamento obligatorio';
      return;
    }
    const body = {
      nombre: this.form.nombre!.trim(),
      descripcion: this.form.descripcion || '',
      activo: this.form.activo !== false
    };
    const req = this.form.id
      ? this.medicamentoService.actualizar({ id: this.form.id, ...body })
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
      next: () => { this.mensaje = 'Medicamento desactivado'; this.cargar(); }
    });
  }

  activar(m: Medicamento): void {
    this.medicamentoService.activar(m).subscribe({
      next: () => { this.mensaje = 'Medicamento activado'; this.cargar(); }
    });
  }
}
