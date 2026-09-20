import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-config-hub',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './config-hub.html',
  styleUrls: ['./config-hub.css', '../shared/config-shared.css']
})
export class ConfigHubComponent {
  readonly hubCards = [
    { path: 'sedes', title: 'Sedes', desc: 'Locales del policlínico', icon: '🏥' },
    { path: 'especialidades', title: 'Especialidades', desc: 'Catálogo clínico', icon: '🩺' },
    { path: 'medicos', title: 'Médicos', desc: 'Vinculación CMP / sede', icon: '👨‍⚕️' },
    { path: 'usuarios', title: 'Usuarios', desc: 'Cuentas y roles', icon: '👤' },
    { path: 'medicamentos', title: 'Medicamentos', desc: 'Catálogo para recetas', icon: '💊' }
  ];
}
