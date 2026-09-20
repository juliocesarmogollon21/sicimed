import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Medico } from '../modelos/medico';

@Injectable({ providedIn: 'root' })
export class MedicoService {
  constructor(private http: HttpClient) {}
  listar(sedeId?: number | null, especialidadId?: number | null): Observable<Medico[]> {
    let params = new HttpParams();
    if (sedeId != null) params = params.set('sedeId', sedeId);
    if (especialidadId != null) params = params.set('especialidadId', especialidadId);
    return this.http.get<Medico[]>(`${environment.apiUrl}/medicos`, { params });
  }
  listarTodos(): Observable<Medico[]> {
    return this.http.get<Medico[]>(`${environment.apiUrl}/medicos`, { params: { all: '1' } });
  }
  crear(m: Partial<Medico> & { usuarioId: number; especialidadId: number; sedeId: number }): Observable<Medico> {
    return this.http.post<Medico>(`${environment.apiUrl}/medicos`, m);
  }
  actualizar(m: Medico): Observable<Medico> {
    return this.http.put<Medico>(`${environment.apiUrl}/medicos/${m.id}`, m);
  }
  desactivar(id: number): Observable<{ ok: boolean }> {
    return this.http.delete<{ ok: boolean }>(`${environment.apiUrl}/medicos/${id}`);
  }
  activar(m: Medico): Observable<Medico> {
    return this.actualizar({ ...m, activo: true });
  }
  agenda(medicoId: number, fecha?: string): Observable<unknown[]> {
    let params = new HttpParams();
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get<unknown[]>(`${environment.apiUrl}/medicos/${medicoId}/agenda`, { params });
  }
}
