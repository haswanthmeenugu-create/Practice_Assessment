import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AnalyticsDashboardComponent, compact } from './analytics-dashboard.component';
import { Distribution, GroupPay, Overview } from '../core/models';

const overview: Overview = {
  baseCurrency: 'USD', activeHeadcount: 3, totalPayroll: 300000,
  averageSalary: 100000, medianSalary: 100000, minSalary: 50000, maxSalary: 150000,
};
const distribution: Distribution = {
  baseCurrency: 'USD', buckets: [{ from: 50000, to: 100000, count: 2 }, { from: 100000, to: 150000, count: 1 }],
};
const byCountry: GroupPay = {
  dimension: 'COUNTRY', baseCurrency: 'USD',
  groups: [
    { key: 'IN', headcount: 1, totalPay: 50000, averagePay: 50000, minPay: 50000, maxPay: 50000 },
    { key: 'US', headcount: 2, totalPay: 250000, averagePay: 125000, minPay: 100000, maxPay: 150000 },
  ],
};

describe('AnalyticsDashboardComponent', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalyticsDashboardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads overview, distribution and country breakdown, then renders KPIs', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    fixture.detectChanges();

    http.expectOne('/api/analytics/overview').flush(overview);
    http.expectOne('/api/analytics/distribution').flush(distribution);
    http.expectOne('/api/analytics/by/country').flush(byCountry);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Active headcount');
    expect(text).toContain('$300,000');
    expect(fixture.componentInstance.loading()).toBeFalse();
  });

  it('orders non-level groups by average pay descending', () => {
    const fixture = TestBed.createComponent(AnalyticsDashboardComponent);
    fixture.detectChanges();
    http.expectOne('/api/analytics/overview').flush(overview);
    http.expectOne('/api/analytics/distribution').flush(distribution);
    http.expectOne('/api/analytics/by/country').flush(byCountry);

    const keys = fixture.componentInstance.orderedGroups().map((g) => g.key);
    expect(keys).toEqual(['US', 'IN']);
  });

  it('formats compact USD labels', () => {
    expect(compact(950)).toBe('$950');
    expect(compact(125000)).toBe('$125k');
    expect(compact(1_500_000)).toBe('$1.5M');
  });
});
