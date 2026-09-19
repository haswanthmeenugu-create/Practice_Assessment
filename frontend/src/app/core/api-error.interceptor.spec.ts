import { HttpErrorResponse } from '@angular/common/http';
import { describe as describeError } from './api-error.interceptor';

describe('api-error describe()', () => {
  it('joins field validation errors', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: {
        timestamp: '', status: 400, error: 'Bad Request',
        message: [{ field: 'email', message: 'must be a well-formed email address' }],
      },
    });
    expect(describeError(err)).toBe('email: must be a well-formed email address');
  });

  it('uses the backend message when it is a string', () => {
    const err = new HttpErrorResponse({
      status: 409,
      error: { timestamp: '', status: 409, error: 'Conflict', message: 'Email already in use: a@b.c' },
    });
    expect(describeError(err)).toBe('Email already in use: a@b.c');
  });

  it('explains a network failure', () => {
    const err = new HttpErrorResponse({ status: 0 });
    expect(describeError(err)).toContain('Cannot reach the server');
  });
});
