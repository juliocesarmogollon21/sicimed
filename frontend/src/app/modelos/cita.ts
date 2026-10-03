export interface Cita {
  id: number;
  medicoId: number;
  medicoNombre: string;
  pacienteId: number;
  pacienteNombre: string;
  sedeId: number;
  sedeNombre: string;
  especialidad: string;
  fecha: string;
  hora: string;
  estado: string;
  motivo?: string;
  diagnostico?: string;
}

export interface CitaRequest {
  medicoId: number;
  pacienteId: number;
  sedeId: number;
  fecha: string;
  hora: string;
  motivo?: string;
}
