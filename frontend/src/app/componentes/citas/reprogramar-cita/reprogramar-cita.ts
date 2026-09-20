import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SedeService } from '../../../servicios/sede.service';
import { EspecialidadService } from '../../../servicios/especialidad.service';
import { MedicoService } from '../../../servicios/medico.service';
import { CitaService } from '../../../servicios/cita.service';
import { PacienteService } from '../../../servicios/paciente.service';
import { AuthService } from '../../../servicios/auth.service';
import { Sede } from '../../../modelos/sede';
import { Especialidad } from '../../../modelos/especialidad';
import { Medico } from '../../../modelos/medico';
import { Cita } from '../../../modelos/cita';

@Component({
  selector: 'app-reprogramar-cita',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reprogramar-cita.html',
  styleUrl: './reprogramar-cita.css'
})
export class ReprogramarCitaComponent implements OnInit {
  citaId: number | null = null;
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
  cargando = true;

  constructor(
    private route: ActivatedRoute,
    private sedeService: SedeService,
    private especialidadService: EspecialidadService,
    private medicoService: MedicoService,
    private citaService: CitaService,
    private pacienteService: PacienteService,
    public auth: AuthService,
    private router: Router
  ) {}

  get esRecepcion(): boolean {
    return this.auth.hasRole('RECEPCIONISTA', 'ADMIN');
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error = 'Cita no válida';
      this.cargando = false;
      return;
    }
    this.citaId = id;
    this.sedeService.listar().subscribe(d => this.sedes = d);
    this.especialidadService.listar().subscribe(d => this.especialidades = d);
    this.citaService.obtener(id).subscribe({
      next: (c: Cita) => {
        if (c.estado !== 'PENDIENTE') {
          this.error = 'Solo se pueden reprogramar citas pendientes';
          this.cargando = false;
          return;
        }
        this.pacienteId = c.pacienteId;
        this.pacienteNombre = c.pacienteNombre;
        this.sedeId = c.sedeId;
        this.medicoId = c.medicoId;
        this.fecha = c.fecha;
        this.hora = (c.hora || '').substring(0, 5);
        this.motivo = c.motivo || '';
        this.medicoService.listar(this.sedeId, null).subscribe(meds => {
          this.medicos = meds;
          const m = meds.find(x => x.id === this.medicoId);
          if (m?.especialidadId) this.especialidadId = m.especialidadId;
          this.cargarHoras(true);
          this.cargando = false;
        });
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'No se pudo cargar la cita';
        this.cargando = false;
      }
    });
  }

  buscarPaciente(): void {
    if (!this.esRecepcion) return;
    this.error = '';
    if (!this.dniBusqueda.trim()) {
      this.error = 'Ingrese DNI';
      return;
    }
    this.pacienteService.buscarPorDni(this.dniBusqueda.trim()).subscribe({
      next: p => {
        this.pacienteId = p.id;
        this.pacienteNombre = p.nombreCompleto;
      },
      error: (err: any) => this.error = err?.error?.message || 'Paciente no encontrado'
    });
  }

  onFiltroChange(): void {
    this.medicoId = null;
    this.horas = [];
    this.hora = '';
    this.medicoService.listar(this.sedeId, this.especialidadId).subscribe(d => this.medicos = d);
  }

  onFechaMedicoChange(): void {
    this.cargarHoras(false);
  }

  private cargarHoras(keepHora: boolean): void {
    const prev = keepHora ? this.hora : '';
    this.horas = [];
    if (!keepHora) this.hora = '';
    if (this.medicoId && this.fecha && this.citaId) {
      this.citaService.disponibilidad(this.medicoId, this.fecha, this.citaId).subscribe({
        next: h => {
          this.horas = h.map(x => x.length >= 5 ? x.substring(0, 5) : x);
          if (keepHora && prev && !this.horas.includes(prev)) {
            this.horas = [prev, ...this.horas];
          }
          if (keepHora) this.hora = prev;
        },
        error: (err: any) => this.error = err?.error?.message || 'No se pudo cargar disponibilidad'
      });
    }
  }

  guardar(): void {
    this.error = '';
    this.ok = '';
    if (!this.citaId || !this.pacienteId || !this.medicoId || !this.sedeId || !this.fecha || !this.hora) {
      this.error = 'Complete todos los campos';
      return;
    }
    const horaNorm = this.hora.length === 5 ? this.hora + ':00' : this.hora;
    this.citaService.actualizar(this.citaId, {
      medicoId: this.medicoId,
      pacienteId: this.pacienteId,
      sedeId: this.sedeId,
      fecha: this.fecha,
      hora: horaNorm,
      motivo: this.motivo
    }).subscribe({
      next: () => {
        this.ok = 'Cita reprogramada';
        setTimeout(() => this.router.navigate(['/citas']), 700);
      },
      error: (err: any) => {
        if (err.status === 409) {
          this.error = err?.error?.message || 'Horario ocupado (conflicto 409)';
        } else {
          this.error = err?.error?.message || 'No se pudo reprogramar';
        }
      }
    });
  }
}
