export interface Medicamento {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
}

export interface RecetaItem {
  medicamentoId?: number | null;
  nombre: string;
  dosis: string;
  frecuencia: string;
}

export interface Receta {
  id?: number;
  citaId: number;
  indicaciones: string;
  medicamentos: string;
  creadoEn?: string;
}
