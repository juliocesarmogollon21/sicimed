import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-config-hub',
  imports: [RouterLink],
  templateUrl: './config-hub.html',
  styleUrls: ['./config-hub.css', '../shared/config-shared.css']
})
export class ConfigHubComponent {
  readonly hubCards = [
    { path: 'sedes', title: 'Sedes', desc: 'Locales del policlínico' },
    { path: 'especialidades', title: 'Especialidades', desc: 'Catálogo clínico' },
    { path: 'medicos', title: 'Médicos', desc: 'Vinculación CMP y sede' },
    { path: 'usuarios', title: 'Usuarios', desc: 'Cuentas y roles' },
    { path: 'medicamentos', title: 'Medicamentos', desc: 'Catálogo para recetas' }
  ];
}
