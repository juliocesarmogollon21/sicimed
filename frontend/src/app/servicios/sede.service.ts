import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Sede } from '../modelos/sede';

@Injectable({ providedIn: 'root' })
export class SedeService {
  constructor(private http: HttpClient) {}
  listar(): Observable<Sede[]> {
    return this.http.get<Sede[]>(`${environment.apiUrl}/sedes`);
  }
  listarTodas(): Observable<Sede[]> {
    return this.http.get<Sede[]>(`${environment.apiUrl}/sedes`, { params: { all: '1' } });
  }
  crear(s: Partial<Sede>): Observable<Sede> {
    return this.http.post<Sede>(`${environment.apiUrl}/sedes`, s);
  }
  actualizar(s: Sede): Observable<Sede> {
    return this.http.put<Sede>(`${environment.apiUrl}/sedes`, s);
  }
  desactivar(id: number): Observable<{ ok: boolean }> {
    return this.http.delete<{ ok: boolean }>(`${environment.apiUrl}/sedes`, { params: { id } });
  }
  activar(s: Sede): Observable<Sede> {
    return this.actualizar({ ...s, activo: true });
  }
}
