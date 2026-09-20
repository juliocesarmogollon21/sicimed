import { Routes } from '@angular/router';
import { LoginComponent } from './componentes/auth/login/login';
import { RegistroComponent } from './componentes/auth/registro/registro';
import { DashboardComponent } from './componentes/dashboard/dashboard';
import { ListaCitasComponent } from './componentes/citas/lista-citas/lista-citas';
import { NuevaCitaComponent } from './componentes/citas/nueva-cita/nueva-cita';
import { ReprogramarCitaComponent } from './componentes/citas/reprogramar-cita/reprogramar-cita';
import { AgendaMedicoComponent } from './componentes/medico/agenda-medico/agenda-medico';
import { ConfigHubComponent } from './componentes/configuracion/hub/config-hub';
import { ConfigSedesComponent } from './componentes/configuracion/sedes/config-sedes';
import { ConfigEspecialidadesComponent } from './componentes/configuracion/especialidades/config-especialidades';
import { ConfigMedicosComponent } from './componentes/configuracion/medicos/config-medicos';
import { ConfigUsuariosComponent } from './componentes/configuracion/usuarios/config-usuarios';
import { ConfigMedicamentosComponent } from './componentes/configuracion/medicamentos/config-medicamentos';
import { authGuard } from './servicios/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'citas', component: ListaCitasComponent, canActivate: [authGuard] },
  { path: 'nueva-cita', component: NuevaCitaComponent, canActivate: [authGuard] },
  { path: 'reprogramar-cita/:id', component: ReprogramarCitaComponent, canActivate: [authGuard] },
  { path: 'agenda-medico', component: AgendaMedicoComponent, canActivate: [authGuard] },
  {
    path: 'admin',
    canActivate: [authGuard],
    children: [
      { path: '', component: ConfigHubComponent },
      { path: 'sedes', component: ConfigSedesComponent },
      { path: 'especialidades', component: ConfigEspecialidadesComponent },
      { path: 'medicos', component: ConfigMedicosComponent },
      { path: 'usuarios', component: ConfigUsuariosComponent },
      { path: 'medicamentos', component: ConfigMedicamentosComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];