import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { MedicoService } from '../../../servicios/medico.service';
import { CitaService } from '../../../servicios/cita.service';
import { PacienteService } from '../../../servicios/paciente.service';
import { AuthService } from '../../../servicios/auth.service';
import { Sede } from '../../../modelos/sede';
import { Especialidad } from '../../../modelos/especialidad';
import { Medico } from '../../../modelos/medico';

@Component({
  selector: 'app-nueva-cita',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './nueva-cita.html',
  styleUrl: './nueva-cita.css'
})
export class NuevaCitaComponent implements OnInit {
  sedes: Sede[] = [];
  especialidades: Especialidad[] = [];
  medicos: Medico[] = [];
  horas: string[] = [];

  sedeId: number | null = null;
  especialidadId: number | null = null;
  medicoId: number | null = null;
  pacienteId: number | null = null;
  dniBusqueda = '';
  pacienteNombre = '';
  fecha = '';
  hora = '';
  motivo = '';
  error = '';
  ok = '';

  constructor(
    private sedeService: SedeService,
    private especialidadService: EspecialidadService,
    private medicoService: MedicoService,
    private citaService: CitaService,
    private pacienteService: PacienteService,
    public auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.sedeService.listar().subscribe(d => this.sedes = d);
    this.especialidadService.listar().subscribe(d => this.especialidades = d);
    const session = this.auth.session();
    if (session?.pacienteId) {
      this.pacienteId = session.pacienteId;
      this.pacienteNombre = session.nombreCompleto;
    }
  }

  get esRecepcion(): boolean {
    return this.auth.hasRole('RECEPCIONISTA', 'ADMIN');
  }

  buscarPaciente(): void {
    this.error = '';
    if (!this.dniBusqueda.trim()) {
      this.error = 'Ingrese DNI';
      return;
    }
    this.pacienteService.buscarPorDni(this.dniBusqueda.trim()).subscribe({
      next: p => {
        this.pacienteId = p.id;
        this.pacienteNombre = p.nombreCompleto;
        this.ok = `Paciente: ${p.nombreCompleto}`;
      },
      error: err => {
        this.pacienteId = null;
        this.pacienteNombre = '';
        this.error = err?.error?.message || 'Paciente no encontrado';
      }
    });
  }

  onFiltroChange(): void {
    this.medicoId = null;
    this.horas = [];
    this.hora = '';
    this.medicoService.listar(this.sedeId, this.especialidadId).subscribe(d => this.medicos = d);
  }

  onFechaMedicoChange(): void {
    this.horas = [];
    this.hora = '';
    if (this.medicoId && this.fecha) {
      this.citaService.disponibilidad(this.medicoId, this.fecha).subscribe({
        next: h => this.horas = h,
        error: err => this.error = err?.error?.message || 'No se pudo cargar disponibilidad'
      });
    }
  }

  guardar(): void {
    this.error = '';
    this.ok = '';
    if (!this.pacienteId) {
      this.error = this.esRecepcion ? 'Busque al paciente por DNI' : 'Sesión de paciente requerida';
      return;
    }
    if (!this.medicoId || !this.sedeId || !this.fecha || !this.hora) {
      this.error = 'Complete sede, especialidad, médico, fecha y hora';
      return;
    }
    this.citaService.crear({
      medicoId: this.medicoId,
      pacienteId: this.pacienteId,
      sedeId: this.sedeId,
      fecha: this.fecha,
      hora: this.hora.length === 5 ? this.hora + ':00' : this.hora,
      motivo: this.motivo
    }).subscribe({
      next: () => {
        this.ok = 'Cita registrada';
        setTimeout(() => this.router.navigate(['/citas']), 800);
      },
      error: err => {
        if (err.status === 409) {
          this.error = err?.error?.message || 'Horario ocupado (conflicto 409)';
        } else {
          this.error = err?.error?.message || 'Error al registrar cita';
        }
      }
    });
  }
}
