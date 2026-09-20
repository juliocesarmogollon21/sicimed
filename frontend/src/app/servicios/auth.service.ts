import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from '../modelos/login-response';

export interface RegisterRequest {
  username: string;
  password: string;
  nombreCompleto: string;
  email: string;
  dni: string;
  telefono: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly key = 'sicimed_auth';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(tap(res => localStorage.setItem(this.key, JSON.stringify(res))));
  }

  register(data: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/register`, data)
      .pipe(tap(res => localStorage.setItem(this.key, JSON.stringify(res))));
  }

  logout(): void {
    localStorage.removeItem(this.key);
  }

  getToken(): string | null {
    return this.session()?.token ?? null;
  }

  session(): LoginResponse | null {
    const raw = localStorage.getItem(this.key);
    return raw ? JSON.parse(raw) as LoginResponse : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  rol(): string | null {
    return this.session()?.rol ?? null;
  }

  hasRole(...roles: string[]): boolean {
    const r = this.rol();
    return !!r && roles.includes(r);
  }
}
