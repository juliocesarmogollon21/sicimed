import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UsuarioDto {
  id?: number;
  username: string;
  password?: string;
  nombreCompleto: string;
  email?: string;
  rol: string;
  activo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private http: HttpClient) {}
  listar(): Observable<UsuarioDto[]> {
    return this.http.get<UsuarioDto[]>(`${environment.apiUrl}/usuarios`);
  }
  crear(u: UsuarioDto): Observable<UsuarioDto> {
    return this.http.post<UsuarioDto>(`${environment.apiUrl}/usuarios`, u);
  }
  actualizar(u: UsuarioDto): Observable<UsuarioDto> {
    return this.http.put<UsuarioDto>(`${environment.apiUrl}/usuarios`, u);
  }
  desactivar(id: number): Observable<{ ok: boolean }> {
    return this.http.delete<{ ok: boolean }>(`${environment.apiUrl}/usuarios`, { params: { id } });
  }
  activar(u: UsuarioDto): Observable<UsuarioDto> {
    const { password, ...rest } = u;
    return this.actualizar({ ...rest, activo: true });
  }
}
