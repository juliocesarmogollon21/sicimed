import { Component, input } from '@angular/core';

/** Estado de una cita como badge nativo de Bootstrap, con bindings [class.x] (sin ngClass). */
@Component({
  selector: 'app-estado-badge',
  imports: [],
  templateUrl: './estado-badge.html'
})
export class EstadoBadgeComponent {
  estado = input.required<string>();

  get conocido(): boolean {
    return ['PENDIENTE', 'ATENDIDO', 'CANCELADO'].includes(this.estado());
  }
}