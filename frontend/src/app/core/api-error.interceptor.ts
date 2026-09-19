import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './models';

/**
 * Surfaces API failures as a snackbar with the backend's message, then
 * re-throws so callers can still react (e.g. keep a dialog open).
 */
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      snackBar.open(describe(err), 'Dismiss', { duration: 6000 });
      return throwError(() => err);
    }),
  );
};

export function describe(err: HttpErrorResponse): string {
  const body = err.error as ApiError | undefined;
  if (body && typeof body === 'object' && 'message' in body) {
    if (Array.isArray(body.message)) {
      return body.message.map((m) => `${m.field}: ${m.message}`).join('; ');
    }
    if (typeof body.message === 'string') {
      return body.message;
    }
  }
  if (err.status === 0) {
    return 'Cannot reach the server. Is the backend running on port 8080?';
  }
  return `Request failed (${err.status})`;
}
