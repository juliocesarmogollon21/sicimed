import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CitaService } from '../../../servicios/cita.service';
import { AuthService } from '../../../servicios/auth.service';
import { Cita } from '../../../modelos/cita';
import { Receta, RecetaItem } from '../../../modelos/medicamento';

@Component({
  selector: 'app-lista-citas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './lista-citas.html',
  styleUrl: './lista-citas.css'
})
export class ListaCitasComponent implements OnInit {
  citas: Cita[] = [];
  dni = '';
  error = '';
  mensaje = '';
  /** Calendario semana por defecto */
  vista: 'calendario' | 'tabla' = 'calendario';

  /** Lunes de la semana visible (00:00 local) */
  semanaInicio = new Date();
  horas: number[] = [];

  // Modal receta
  recetaVisible = false;
  recetaCita: Cita | null = null;
  recetaData: Receta | null = null;
  recetaItems: RecetaItem[] = [];
  recetaCargando = false;

  // Modal reprogramar
  reprogVisible = false;
  reprogCita: Cita | null = null;
  reprogFecha = '';
  reprogHora = '';

  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  constructor(private citaService: CitaService, public auth: AuthService) {
    for (let h = 8; h <= 20; h++) this.horas.push(h);
  }

  ngOnInit(): void {
    this.irHoy();
    this.cargar();
  }

  hoyIso(): string {
    return this.toIso(new Date());
  }

  toIso(d: Date): string {
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }

  /** Lunes de la semana que contiene `d` */
  lunesDe(d: Date): Date {
    const x = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const dow = (x.getDay() + 6) % 7; // lun=0 … dom=6
    x.setDate(x.getDate() - dow);
    return x;
  }

  irHoy(): void {
    this.semanaInicio = this.lunesDe(new Date());
  }

  semanaAnterior(): void {
    const x = new Date(this.semanaInicio);
    x.setDate(x.getDate() - 7);
    this.semanaInicio = x;
  }

  semanaSiguiente(): void {
    const x = new Date(this.semanaInicio);
    x.setDate(x.getDate() + 7);
    this.semanaInicio = x;
  }

  diasDeSemana(): Date[] {
    const out: Date[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.semanaInicio);
      d.setDate(d.getDate() + i);
      out.push(d);
    }
    return out;
  }

  etiquetaSemana(): string {
    const dias = this.diasDeSemana();
    const a = dias[0];
    const b = dias[6];
    const fmt = (d: Date) =>
      `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`;
    return `${fmt(a)} – ${fmt(b)} ${b.getFullYear()}`;
  }

  esHoy(d: Date): boolean {
    return this.toIso(d) === this.hoyIso();
  }

  cargar(): void {
    this.error = '';
    const filtro = this.auth.hasRole('RECEPCIONISTA', 'ADMIN') && this.dni.trim()
      ? this.dni.trim() : undefined;
    this.citaService.listar(filtro).subscribe({
      next: data => this.citas = data,
      error: err => this.error = err?.error?.message || 'Error al cargar citas'
    });
  }

  buscarDni(): void {
    this.cargar();
  }

  setVista(v: 'tabla' | 'calendario'): void {
    this.vista = v;
  }

  normalizarHora(hora: string | undefined | null): string {
    if (!hora) return '';
    const p = hora.trim().split(':');
    if (p.length < 2) return hora;
    return `${p[0].padStart(2, '0')}:${p[1].padStart(2, '0')}`;
  }

  horaNumero(hora: string | undefined | null): number {
    const n = this.normalizarHora(hora);
    if (!n) return -1;
    return parseInt(n.split(':')[0], 10);
  }

  citasEnCelda(dia: Date, hora: number): Cita[] {
    const iso = this.toIso(dia);
    return this.citas.filter(c => c.fecha === iso && this.horaNumero(c.hora) === hora);
  }

  citasDelDiaIso(iso: string): Cita[] {
    return this.citas.filter(c => c.fecha === iso);
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'cal-bar-pendiente';
      case 'ATENDIDO': return 'cal-bar-atendido';
      case 'CANCELADO': return 'cal-bar-cancelado';
      default: return 'cal-bar-otro';
    }
  }

  badgeEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'text-bg-warning';
      case 'ATENDIDO': return 'text-bg-success';
      case 'CANCELADO': return 'text-bg-secondary';
      default: return 'text-bg-light';
    }
  }

  cancelar(c: Cita): void {
    if (!confirm(`¿Cancelar cita #${c.id}?`)) return;
    this.citaService.cancelar(c.id).subscribe({
      next: () => {
        this.mensaje = `Cita #${c.id} cancelada`;
        this.cargar();
      },
      error: err => this.error = err?.error?.message || 'No se pudo cancelar'
    });
  }

  abrirReprogramar(c: Cita): void {
    this.reprogCita = c;
    this.reprogFecha = c.fecha;
    this.reprogHora = this.normalizarHora(c.hora);
    this.reprogVisible = true;
  }

  cerrarReprogramar(): void {
    this.reprogVisible = false;
    this.reprogCita = null;
  }

  confirmarReprogramar(): void {
    if (!this.reprogCita) return;
    const c = this.reprogCita;
    this.citaService.reprogramar(c.id, {
      medicoId: c.medicoId,
      pacienteId: c.pacienteId,
      sedeId: c.sedeId,
      fecha: this.reprogFecha,
      hora: this.reprogHora,
      motivo: c.motivo
    }).subscribe({
      next: () => {
        this.mensaje = `Cita #${c.id} reprogramada`;
        this.cerrarReprogramar();
        this.cargar();
      },
      error: err => this.error = err?.error?.message || 'No se pudo reprogramar'
    });
  }

  puedeVerReceta(c: Cita): boolean {
    return c.estado === 'ATENDIDO' && this.auth.hasRole('PACIENTE', 'ADMIN', 'MEDICO', 'RECEPCIONISTA');
  }

  abrirReceta(c: Cita): void {
    this.recetaCita = c;
    this.recetaData = null;
    this.recetaItems = [];
    this.recetaVisible = true;
    this.recetaCargando = true;
    this.citaService.obtenerReceta(c.id).subscribe({
      next: r => {
        this.recetaData = r;
        this.recetaItems = this.parseMedicamentos(r.medicamentos);
        this.recetaCargando = false;
      },
      error: err => {
        this.recetaCargando = false;
        this.error = err?.error?.message || 'No se pudo cargar la receta';
      }
    });
  }

  cerrarReceta(): void {
    this.recetaVisible = false;
    this.recetaCita = null;
  }

  parseMedicamentos(raw: string | undefined | null): RecetaItem[] {
    if (!raw || !raw.trim()) return [];
    const t = raw.trim();
    if (t.startsWith('[')) {
      try {
        const arr = JSON.parse(t) as RecetaItem[];
        if (Array.isArray(arr)) return arr;
      } catch { /* legado */ }
    }
    // Texto legado: una línea = un medicamento
    return t.split(/\r?\n/).filter(l => l.trim()).map(l => ({
      nombre: l.trim(), dosis: '', frecuencia: ''
    }));
  }
}
