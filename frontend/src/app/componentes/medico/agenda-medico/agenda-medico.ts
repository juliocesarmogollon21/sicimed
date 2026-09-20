import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MedicoService } from '../../../servicios/medico.service';
import { CitaService } from '../../../servicios/cita.service';
import { MedicamentoService } from '../../../servicios/medicamento.service';
import { AuthService } from '../../../servicios/auth.service';
import { Medico } from '../../../modelos/medico';
import { Cita } from '../../../modelos/cita';
import { Medicamento, RecetaItem } from '../../../modelos/medicamento';

@Component({
  selector: 'app-agenda-medico',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agenda-medico.html',
  styleUrl: './agenda-medico.css'
})
export class AgendaMedicoComponent implements OnInit {
  medicos: Medico[] = [];
  catalogo: Medicamento[] = [];
  citas: Cita[] = [];
  medicoId: number | null = null;
  fecha = '';
  error = '';
  mensaje = '';
  diagnosticoTexto: Record<number, string> = {};
  recetaInd: Record<number, string> = {};
  recetaItems: Record<number, RecetaItem[]> = {};
  cargando = false;

  /** Vista: por atender | ya atendidos */
  panel: 'por_atender' | 'atendidos' = 'por_atender';
  filtroPaciente = '';
  orden: 'hora' | 'paciente' = 'hora';

  /** Modal receta (opcional, solo atendidos) */
  showRecetaModal = false;
  citaReceta: Cita | null = null;
  recetaGuardadaMsg = '';

  constructor(
    private medicoService: MedicoService,
    private citaService: CitaService,
    private medicamentoService: MedicamentoService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.fecha = this.hoyIso();
    this.medicamentoService.listar(false).subscribe({
      next: d => this.catalogo = d,
      error: () => {}
    });
    this.medicoService.listar().subscribe(d => {
      this.medicos = d;
      const mid = this.auth.session()?.medicoId;
      if (mid) this.medicoId = mid;
      if (this.medicoId) this.consultar();
    });
  }

  hoyIso(): string {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }

  esMedicoSesion(): boolean {
    return this.auth.hasRole('MEDICO') && !!this.auth.session()?.medicoId;
  }

  consultar(): void {
    this.error = '';
    if (!this.medicoId) {
      this.error = 'Seleccione un médico';
      return;
    }
    this.cargando = true;
    this.medicoService.agenda(this.medicoId, this.fecha || undefined).subscribe({
      next: data => {
        const lista = data as Cita[];
        this.citas = lista;
        this.prefillFormularios(lista);
        this.cargando = false;
      },
      error: err => {
        this.cargando = false;
        this.error = err?.error?.message || 'Error al consultar agenda';
      }
    });
  }

  private prefillFormularios(lista: Cita[]): void {
    for (const c of lista) {
      if (c.diagnostico && !this.diagnosticoTexto[c.id]) {
        this.diagnosticoTexto[c.id] = c.diagnostico;
      }
      if (!this.recetaItems[c.id]) {
        this.recetaItems[c.id] = [this.filaVacia()];
      }
    }
  }

  get citasFiltradas(): Cita[] {
    const q = this.filtroPaciente.trim().toLowerCase();
    let list = this.citas.filter(c => {
      if (this.panel === 'por_atender') return c.estado === 'PENDIENTE';
      if (this.panel === 'atendidos') return c.estado === 'ATENDIDO';
      return true;
    });
    if (q) {
      list = list.filter(c => (c.pacienteNombre || '').toLowerCase().includes(q));
    }
    list = [...list].sort((a, b) => {
      if (this.orden === 'paciente') {
        return (a.pacienteNombre || '').localeCompare(b.pacienteNombre || '', 'es');
      }
      const ha = (a.hora || '').substring(0, 5);
      const hb = (b.hora || '').substring(0, 5);
      return ha.localeCompare(hb);
    });
    return list;
  }

  get countPendientes(): number {
    return this.citas.filter(c => c.estado === 'PENDIENTE').length;
  }

  get countAtendidos(): number {
    return this.citas.filter(c => c.estado === 'ATENDIDO').length;
  }

  filaVacia(): RecetaItem {
    return { medicamentoId: null, nombre: '', dosis: '', frecuencia: '' };
  }

  parseItems(raw: string | undefined | null): RecetaItem[] {
    if (!raw || !raw.trim()) return [];
    const t = raw.trim();
    if (t.startsWith('[')) {
      try {
        const arr = JSON.parse(t) as RecetaItem[];
        if (Array.isArray(arr)) {
          return arr.map(x => ({
            medicamentoId: x.medicamentoId ?? null,
            nombre: x.nombre || '',
            dosis: x.dosis || '',
            frecuencia: x.frecuencia || ''
          }));
        }
      } catch { /* legado */ }
    }
    return t.split(/\r?\n/).filter(l => l.trim()).map(l => ({
      medicamentoId: null, nombre: l.trim(), dosis: '', frecuencia: ''
    }));
  }

  itemsDe(citaId: number): RecetaItem[] {
    if (!this.recetaItems[citaId]) this.recetaItems[citaId] = [this.filaVacia()];
    return this.recetaItems[citaId];
  }

  agregarFila(citaId: number): void {
    this.itemsDe(citaId).push(this.filaVacia());
  }

  quitarFila(citaId: number, idx: number): void {
    const arr = this.itemsDe(citaId);
    if (arr.length <= 1) {
      arr[0] = this.filaVacia();
      return;
    }
    arr.splice(idx, 1);
  }

  onPickCatalogo(citaId: number, idx: number, medId: string): void {
    const id = medId ? Number(medId) : null;
    const fila = this.itemsDe(citaId)[idx];
    fila.medicamentoId = id;
    if (id) {
      const m = this.catalogo.find(x => x.id === id);
      if (m) fila.nombre = m.nombre;
    }
  }

  guardarDiagnostico(c: Cita): void {
    const texto = (this.diagnosticoTexto[c.id] || '').trim();
    if (!texto) { this.error = 'Ingrese diagnóstico'; return; }
    this.error = '';
    this.citaService.diagnostico(c.id, texto).subscribe({
      next: () => {
        this.mensaje = `Cita #${c.id} atendida. La receta es opcional: puede agregarla después en Atendidos.`;
        this.consultar();
      },
      error: err => this.error = err?.error?.message || 'Error diagnóstico'
    });
  }

  abrirReceta(c: Cita): void {
    this.citaReceta = c;
    this.recetaGuardadaMsg = '';
    this.error = '';
    if (!this.recetaItems[c.id]) this.recetaItems[c.id] = [this.filaVacia()];
    this.citaService.obtenerReceta(c.id).subscribe({
      next: r => {
        this.recetaInd[c.id] = r.indicaciones || '';
        const items = this.parseItems(r.medicamentos);
        this.recetaItems[c.id] = items.length ? items : [this.filaVacia()];
        this.showRecetaModal = true;
      },
      error: () => {
        this.recetaInd[c.id] = this.recetaInd[c.id] || '';
        this.recetaItems[c.id] = this.recetaItems[c.id] || [this.filaVacia()];
        this.showRecetaModal = true;
      }
    });
  }

  cerrarReceta(): void {
    this.showRecetaModal = false;
    this.citaReceta = null;
  }

  guardarReceta(): void {
    const c = this.citaReceta;
    if (!c) return;
    const ind = (this.recetaInd[c.id] || '').trim();
    const items = this.itemsDe(c.id)
      .map(it => ({
        medicamentoId: it.medicamentoId || null,
        nombre: (it.nombre || '').trim(),
        dosis: (it.dosis || '').trim(),
        frecuencia: (it.frecuencia || '').trim()
      }))
      .filter(it => it.nombre);
    if (!items.length) { this.error = 'Agregue al menos un medicamento'; return; }
    if (!ind) { this.error = 'Ingrese indicaciones'; return; }
    this.error = '';
    this.citaService.receta(c.id, ind, JSON.stringify(items)).subscribe({
      next: () => {
        this.recetaGuardadaMsg = 'Receta guardada';
        this.mensaje = `Receta guardada en cita #${c.id}`;
        setTimeout(() => this.cerrarReceta(), 700);
      },
      error: err => this.error = err?.error?.message || 'Error receta'
    });
  }

  badgeEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'text-bg-warning';
      case 'ATENDIDO': return 'text-bg-success';
      case 'CANCELADO': return 'text-bg-secondary';
      default: return 'text-bg-light';
    }
  }

  puedeAtender(c: Cita): boolean {
    return c.estado !== 'CANCELADO' && this.auth.hasRole('MEDICO', 'ADMIN');
  }
}
