import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Especialidad } from '../modelos/especialidad';

@Injectable({ providedIn: 'root' })
export class EspecialidadService {
  constructor(private http: HttpClient) {}
  listar(): Observable<Especialidad[]> {
    return this.http.get<Especialidad[]>(`${environment.apiUrl}/especialidades`);
  }
  crear(e: Partial<Especialidad>): Observable<Especialidad> {
    return this.http.post<Especialidad>(`${environment.apiUrl}/especialidades`, e);
  }
  actualizar(e: Especialidad): Observable<Especialidad> {
    return this.http.put<Especialidad>(`${environment.apiUrl}/especialidades`, e);
  }
  eliminar(id: number): Observable<{ ok: boolean }> {
    return this.http.delete<{ ok: boolean }>(`${environment.apiUrl}/especialidades`, { params: { id } });
  }
}
