import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmployeeApi } from './employee-api.service';
import { EmployeeSearch } from './models';

describe('EmployeeApi', () => {
  let api: EmployeeApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(EmployeeApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filters that are set', () => {
    const criteria: EmployeeSearch = {
      q: '  hopper ', country: '', department: 'ENGINEERING', level: '', status: '',
      page: 2, size: 50, sort: 'baseSalaryUsd', direction: 'desc',
    };
    api.search(criteria).subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    const p = req.request.params;
    expect(p.get('q')).toBe('hopper');
    expect(p.get('department')).toBe('ENGINEERING');
    expect(p.has('country')).toBeFalse();
    expect(p.has('level')).toBeFalse();
    expect(p.has('status')).toBeFalse();
    expect(p.get('page')).toBe('2');
    expect(p.get('size')).toBe('50');
    expect(p.get('sort')).toBe('baseSalaryUsd');
    expect(p.get('direction')).toBe('desc');
    req.flush({ content: [], page: 2, size: 50, totalElements: 0, totalPages: 0 });
  });

  it('deletes by id', () => {
    api.delete(42).subscribe();
    const req = http.expectOne('/api/employees/42');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
