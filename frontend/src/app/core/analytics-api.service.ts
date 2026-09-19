import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Dimension, Distribution, GroupPay, Overview } from './models';

/** Typed client for /api/analytics. */
@Injectable({ providedIn: 'root' })
export class AnalyticsApi {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/analytics';

  overview(): Observable<Overview> {
    return this.http.get<Overview>(`${this.base}/overview`);
  }

  by(dimension: Dimension): Observable<GroupPay> {
    return this.http.get<GroupPay>(`${this.base}/by/${dimension}`);
  }

  distribution(): Observable<Distribution> {
    return this.http.get<Distribution>(`${this.base}/distribution`);
  }
}
