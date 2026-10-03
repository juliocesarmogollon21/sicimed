import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { Cita, CitaRequest } from '../modelos/cita';
import { Pagina } from '../modelos/pagina';
import { Receta } from '../modelos/medicamento';

@Injectable({ providedIn: 'root' })
export class CitaService {
  constructor(private http: HttpClient) {}

  listar(dni?: string): Observable<Cita[]> {
    return this.listarPaginado(dni).pipe(map(p => p.content));
  }

  listarPaginado(dni?: string, page = 0, size = 50): Observable<Pagina<Cita>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (dni) params = params.set('dni', dni);
    return this.http.get<Pagina<Cita>>(`${environment.apiUrl}/citas`, { params });
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
    void excludeCitaId;
    const params = new HttpParams().set('medicoId', medicoId).set('fecha', fecha);
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