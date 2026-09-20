import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Medicamento } from '../modelos/medicamento';

@Injectable({ providedIn: 'root' })
export class MedicamentoService {
  constructor(private http: HttpClient) {}

  listar(all = false): Observable<Medicamento[]> {
    let params = new HttpParams();
    if (all) params = params.set('all', 'true');
    return this.http.get<Medicamento[]>(`${environment.apiUrl}/medicamentos`, { params });
  }

  crear(body: Partial<Medicamento>): Observable<Medicamento> {
    return this.http.post<Medicamento>(`${environment.apiUrl}/medicamentos`, body);
  }

  actualizar(body: Partial<Medicamento> & { id: number }): Observable<Medicamento> {
    return this.http.put<Medicamento>(`${environment.apiUrl}/medicamentos`, body);
  }

  desactivar(id: number): Observable<{ ok: boolean }> {
    const params = new HttpParams().set('id', id);
    return this.http.delete<{ ok: boolean }>(`${environment.apiUrl}/medicamentos`, { params });
  }

  activar(m: Medicamento): Observable<Medicamento> {
    return this.actualizar({ id: m.id, nombre: m.nombre, descripcion: m.descripcion, activo: true });
  }
}
