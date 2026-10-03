import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CitaService } from '../../../servicios/cita.service';
import { AuthService } from '../../../servicios/auth.service';
import { Cita } from '../../../modelos/cita';
import { Receta, RecetaItem } from '../../../modelos/medicamento';
import { AlertaComponent } from '../../compartido/alerta/alerta';
import { EstadoBadgeComponent } from '../../compartido/estado-badge/estado-badge';

@Component({
  selector: 'app-lista-citas',
  imports: [EstadoBadgeComponent, AlertaComponent, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './lista-citas.html',
  styleUrl: './lista-citas.css'
})
export class ListaCitasComponent implements OnInit {
  citas: Cita[] = [];
  dni = '';
  error = '';
  mensaje = '';

  vista: 'calendario' | 'tabla' = 'calendario';

  semanaInicio = new Date();
  horas: number[] = [];

  recetaVisible = false;
  recetaCita: Cita | null = null;
  recetaData: Receta | null = null;
  recetaItems: RecetaItem[] = [];
  recetaCargando = false;

  reprogVisible = false;
  reprogCita: Cita | null = null;
  reprogForm: FormGroup;
  fechaMinima = this.hoyIso();
  submitted = false;

  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  constructor(
    private fb: FormBuilder,
    private citaService: CitaService,
    public auth: AuthService
  ) {
    for (let h = 8; h <= 20; h++) this.horas.push(h);

    this.reprogForm = this.fb.group({
      fecha: ['', [Validators.required, this.fechaNoPasadaValidator()]],
      hora: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.irHoy();
    this.cargar();
  }

  // ---------- Validación del modal de reprogramación ----------

  private fechaNoPasadaValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const valor = control.value;
      if (!valor) return null;
      return this.esFechaPasada(valor) ? { fechaPasada: true } : null;
    };
  }

  private esFechaPasada(fecha: string): boolean {
    if (!fecha) return true;
    return fecha < this.hoyIso();
  }

  mostrarReprogError(nombre: 'fecha' | 'hora'): boolean {
    const c = this.reprogForm.get(nombre)!;
    return c.invalid && (c.touched || this.submitted);
  }

  mensajeReprogError(nombre: 'fecha' | 'hora'): string {
    const c = this.reprogForm.get(nombre)!;
    if (c.hasError('required')) {
      return nombre === 'fecha' ? 'Selecciona la nueva fecha' : 'Selecciona la nueva hora';
    }
    if (c.hasError('fechaPasada')) return 'No puedes elegir una fecha pasada.';
    return 'Revisa este campo';
  }

  // ---------- Lógica de negocio ----------

  hoyIso(): string {
    return this.toIso(new Date());
  }

  toIso(d: Date): string {
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }

  lunesDe(d: Date): Date {
    const x = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const dow = (x.getDay() + 6) % 7;
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
    return `${fmt(a)} - ${fmt(b)} ${b.getFullYear()}`;
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
    this.submitted = false;
    this.reprogForm.reset({ fecha: c.fecha, hora: this.normalizarHora(c.hora) });
    this.reprogVisible = true;
  }

  cerrarReprogramar(): void {
    this.reprogVisible = false;
    this.reprogCita = null;
  }

  confirmarReprogramar(): void {
    if (!this.reprogCita) return;
    this.submitted = true;
    if (this.reprogForm.invalid) {
      this.reprogForm.markAllAsTouched();
      return;
    }

    const c = this.reprogCita;
    const v = this.reprogForm.getRawValue();
    this.citaService.reprogramar(c.id, {
      medicoId: c.medicoId,
      pacienteId: c.pacienteId,
      sedeId: c.sedeId,
      fecha: String(v['fecha']),
      hora: String(v['hora']),
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
        this.cerrarReceta();
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
      } catch {  }
    }

    return t.split(/\r?\n/).filter(l => l.trim()).map(l => ({
      nombre: l.trim(), dosis: '', frecuencia: ''
    }));
  }
}
