import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PacienteDto {
  id: number;
  usuarioId: number;
  nombreCompleto: string;
  dni: string;
  telefono?: string;
}

@Injectable({ providedIn: 'root' })
export class PacienteService {
  constructor(private http: HttpClient) {}
  buscarPorDni(dni: string): Observable<PacienteDto> {
    return this.http.get<PacienteDto>(`${environment.apiUrl}/pacientes`, { params: { dni } });
  }
}
