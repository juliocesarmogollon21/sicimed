import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
    return this.http.get<Sede[]>(`${environment.apiUrl}/sedes`, { params: { all: 'true' } });
  }
  crear(s: Partial<Sede>): Observable<Sede> {
    return this.http.post<Sede>(`${environment.apiUrl}/sedes`, s);
  }
  actualizar(s: Sede): Observable<Sede> {
    return this.http.put<Sede>(`${environment.apiUrl}/sedes/${s.id}`, s);
  }

  desactivar(id: number): Observable<void> {
    const params = new HttpParams().set('id', id);
    return this.http.delete<void>(`${environment.apiUrl}/sedes/${id}`, { params });
  }
  activar(s: Sede): Observable<Sede> {
    return this.actualizar({ ...s, activo: true });
  }
}
