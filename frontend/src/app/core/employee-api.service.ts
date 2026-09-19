import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee, EmployeeRequest, EmployeeSearch, PageResponse } from './models';

/** Typed client for /api/employees. Components never build URLs themselves. */
@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/employees';

  search(criteria: EmployeeSearch): Observable<PageResponse<Employee>> {
    let params = new HttpParams()
      .set('page', criteria.page)
      .set('size', criteria.size)
      .set('sort', criteria.sort)
      .set('direction', criteria.direction);

    // Only send filters that are actually set so the backend treats the
    // rest as "no filter".
    const optional: Array<[string, string | undefined]> = [
      ['q', criteria.q?.trim()],
      ['country', criteria.country?.trim()],
      ['department', criteria.department || undefined],
      ['level', criteria.level || undefined],
      ['status', criteria.status || undefined],
    ];
    for (const [key, value] of optional) {
      if (value) {
        params = params.set(key, value);
      }
    }
    return this.http.get<PageResponse<Employee>>(this.base, { params });
  }

  get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.base}/${id}`);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.base, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
