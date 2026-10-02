import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }
  const roles = (route.data['roles'] as string[] | undefined) ?? [];
  if (roles.length === 0 || auth.hasRole(...roles)) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};