export interface Medico {
  id: number;
  usuarioId: number;
  nombreCompleto: string;
  especialidadId: number;
  especialidadNombre: string;
  sedeId: number;
  sedeNombre: string;
  cmp?: string;
  activo?: boolean;
}
