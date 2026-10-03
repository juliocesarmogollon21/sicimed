import { Component, input, output } from '@angular/core';

/** Mensaje de exito/error reutilizable: recibe datos con input() y avisa el cierre con output(). */
@Component({
  selector: 'app-alerta',
  imports: [],
  templateUrl: './alerta.html'
})
export class AlertaComponent {
  mensaje = input<string>('');
  tipo = input<'success' | 'danger' | 'warning'>('danger');
  cerrar = output<void>();
}