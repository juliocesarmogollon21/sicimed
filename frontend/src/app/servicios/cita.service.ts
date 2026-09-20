import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Cita, CitaRequest } from '../modelos/cita';
import { Receta } from '../modelos/medicamento';

@Injectable({ providedIn: 'root' })
export class CitaService {
  constructor(private http: HttpClient) {}

  listar(dni?: string): Observable<Cita[]> {
    let params = new HttpParams();
    if (dni) params = params.set('dni', dni);
    return this.http.get<Cita[]>(`${environment.apiUrl}/citas`, { params });
  }

  obtener(id: number): Observable<Cita> {
    return this.http.get<Cita>(`${environment.apiUrl}/citas/${id}`);
  }

  crear(req: CitaRequest): Observable<Cita> {
    return this.http.post<Cita>(`${environment.apiUrl}/citas`, req);
  }

  actualizar(id: number, req: CitaRequest): Observable<Cita> {
    return this.http.put<Cita>(`${environment.apiUrl}/citas/${id}`, req);
  }

  cancelar(id: number): Observable<Cita> {
    return this.http.delete<Cita>(`${environment.apiUrl}/citas/${id}`);
  }

  reprogramar(id: number, req: CitaRequest): Observable<Cita> {
    return this.http.put<Cita>(`${environment.apiUrl}/citas/${id}`, req);
  }

  disponibilidad(medicoId: number, fecha: string, excludeCitaId?: number): Observable<string[]> {
    let params = new HttpParams().set('medicoId', medicoId).set('fecha', fecha);
    if (excludeCitaId != null) params = params.set('excludeCitaId', excludeCitaId);
    return this.http.get<string[]>(`${environment.apiUrl}/citas/disponibilidad`, { params });
  }

  diagnostico(id: number, diagnostico: string): Observable<Cita> {
    return this.http.post<Cita>(`${environment.apiUrl}/citas/${id}/diagnostico`, { diagnostico });
  }

  receta(id: number, indicaciones: string, medicamentos: string): Observable<Receta> {
    return this.http.post<Receta>(`${environment.apiUrl}/citas/${id}/receta`, { indicaciones, medicamentos });
  }

  obtenerReceta(id: number): Observable<Receta> {
    return this.http.get<Receta>(`${environment.apiUrl}/citas/${id}/receta`);
  }
}