import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../servicios/auth.service';
import { CitaService } from '../../servicios/cita.service';
import { LoginResponse } from '../../modelos/login-response';
import { Cita } from '../../modelos/cita';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  user: LoginResponse | null = null;
  totalCitas = 0;
  programadas = 0;
  atendidas = 0;
  canceladas = 0;
  cargandoStats = true;

  constructor(public auth: AuthService, private citaService: CitaService) {}

  ngOnInit(): void {
    this.user = this.auth.session();
    this.citaService.listar().subscribe({
      next: (citas: Cita[]) => {
        this.totalCitas = citas.length;
        this.programadas = citas.filter(c => c.estado === 'PENDIENTE').length;
        this.atendidas = citas.filter(c => c.estado === 'ATENDIDO').length;
        this.canceladas = citas.filter(c => c.estado === 'CANCELADO').length;
        this.cargandoStats = false;
      },
      error: () => {
        this.cargandoStats = false;
      }
    });
  }
}
