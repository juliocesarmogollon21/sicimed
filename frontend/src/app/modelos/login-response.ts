export interface LoginResponse {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: string;
  userId: number;
  medicoId: number | null;
  pacienteId: number | null;
}
