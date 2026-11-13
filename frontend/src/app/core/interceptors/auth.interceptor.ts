import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastService } from '../services/toast.service';
import { environment } from '../../../environments/environment';
import { catchError, switchMap, throwError } from 'rxjs';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const toasts = inject(ToastService);
  const auth = inject(AuthService);
  const credentialedRequest = req.clone({ withCredentials: true });
  const isUnsafe = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method.toUpperCase());
  const request$ = isUnsafe
    ? new HttpClient(inject(HttpBackend)).get<{ requestToken: string }>(`${environment.apiUrl}/auth/csrf`, { withCredentials: true }).pipe(
        switchMap(result => next(credentialedRequest.clone({ setHeaders: { 'X-CSRF-TOKEN': result.requestToken } })))
      )
    : next(credentialedRequest);
  return request$.pipe(
    catchError(error => {
      if (error.status === 401 && req.url.endsWith('/auth/me')) return throwError(() => error);
      if (error.status === 401 && req.url.endsWith('/auth/login')) return throwError(() => error);
      if (error.status === 401) {
        auth.expireSession();
        return throwError(() => error);
      }
      const message = error.error?.message ?? (error.status === 0 ? 'No se pudo conectar con el servidor.' : 'Ocurrió un error. Inténtalo de nuevo.');
      toasts.show(message, 'error');
      return throwError(() => error);
    })
  );
};
