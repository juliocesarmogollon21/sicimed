import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicoService } from '../../../servicios/medico.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { SedeService } from '../../../servicios/sede.service';
import { UsuarioService, UsuarioDto } from '../../../servicios/usuario.service';
import { Medico } from '../../../modelos/medico';
import { Especialidad } from '../../../modelos/especialidad';
import { Sede } from '../../../modelos/sede';

@Component({
  selector: 'app-config-medicos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './config-medicos.html',
  styleUrls: ['./config-medicos.css', '../shared/config-shared.css']
})
export class ConfigMedicosComponent implements OnInit {
  medicos: Medico[] = [];
  especialidades: Especialidad[] = [];
  sedes: Sede[] = [];
  usuariosMedico: UsuarioDto[] = [];
  form: { id?: number; usuarioId?: number; especialidadId?: number; sedeId?: number; cmp?: string; activo?: boolean } = { activo: true };
  modalOpen = false;
  mensaje = '';
  error = '';

  constructor(
    private medicoService: MedicoService,
    private espService: EspecialidadService,
    private sedeService: SedeService,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.medicoService.listarTodos().subscribe({
      next: d => this.medicos = d,
      error: () => this.medicoService.listar().subscribe(d => this.medicos = d)
    });
    this.espService.listar().subscribe({ next: d => this.especialidades = d });
    this.sedeService.listarTodas().subscribe({ next: d => this.sedes = d });
    this.usuarioService.listar().subscribe({
      next: d => this.usuariosMedico = d.filter(u => (u.rol || '').toUpperCase() === 'MEDICO'),
      error: e => this.error = e?.error?.message || 'Error usuarios'
    });
  }

  abrirNuevo(): void {
    this.form = { activo: true };
    this.error = '';
    this.modalOpen = true;
  }

  editar(m: Medico): void {
    this.form = {
      id: m.id,
      usuarioId: m.usuarioId,
      especialidadId: m.especialidadId,
      sedeId: m.sedeId,
      cmp: m.cmp,
      activo: m.activo
    };
    this.error = '';
    this.modalOpen = true;
  }

  cerrar(): void { this.modalOpen = false; }

  guardar(): void {
    if (!this.form.usuarioId || !this.form.especialidadId || !this.form.sedeId) {
      this.error = 'Usuario, especialidad y sede son obligatorios';
      return;
    }
    const body = {
      usuarioId: this.form.usuarioId!,
      especialidadId: this.form.especialidadId!,
      sedeId: this.form.sedeId!,
      cmp: this.form.cmp,
      activo: this.form.activo !== false
    };
    const req = this.form.id
      ? this.medicoService.actualizar({
          id: this.form.id,
          ...body,
          nombreCompleto: '',
          especialidadNombre: '',
          sedeNombre: ''
        })
      : this.medicoService.crear(body);
    req.subscribe({
      next: () => {
        this.mensaje = 'Médico guardado';
        this.modalOpen = false;
        this.cargar();
      },
      error: e => this.error = e?.error?.message || 'Error al guardar médico'
    });
  }

  desactivar(m: Medico): void {
    this.medicoService.desactivar(m.id).subscribe({
      next: () => { this.mensaje = 'Médico desactivado'; this.cargar(); }
    });
  }

  activar(m: Medico): void {
    this.medicoService.activar(m).subscribe({
      next: () => { this.mensaje = 'Médico activado'; this.cargar(); }
    });
  }
}
