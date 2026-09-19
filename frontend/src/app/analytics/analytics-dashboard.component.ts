import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { AnalyticsApi } from '../core/analytics-api.service';
import { Dimension, Distribution, GroupPay, LEVELS, Overview, PayGroup } from '../core/models';
import { BarChartComponent, BarItem } from '../shared/bar-chart.component';
import { ReplaceUnderscorePipe } from '../shared/replace-underscore.pipe';

/**
 * "How does the org pay people" in one screen: headline KPIs, pay by
 * country/department/level, and the salary distribution. All figures come
 * from the API already normalized to USD; the UI does no math beyond layout.
 */
@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe, DecimalPipe, TitleCasePipe, MatCardModule, MatButtonToggleModule, MatTableModule,
    MatProgressBarModule, MatIconModule, MatButtonModule, BarChartComponent, ReplaceUnderscorePipe,
  ],
  templateUrl: './analytics-dashboard.component.html',
  styleUrl: './analytics-dashboard.component.scss',
})
export class AnalyticsDashboardComponent {
  private readonly api = inject(AnalyticsApi);

  readonly loading = signal(true);
  readonly overview = signal<Overview | null>(null);
  readonly distribution = signal<Distribution | null>(null);
  readonly groupPay = signal<GroupPay | null>(null);
  readonly dimension = signal<Dimension>('country');

  readonly groupColumns = ['key', 'headcount', 'averagePay', 'totalPay', 'minPay', 'maxPay'];

  constructor() {
    this.loadAll();
  }

  loadAll(): void {
    this.loading.set(true);
    forkJoin({
      overview: this.api.overview(),
      distribution: this.api.distribution(),
      groups: this.api.by(this.dimension()),
    }).subscribe({
      next: ({ overview, distribution, groups }) => {
        this.overview.set(overview);
        this.distribution.set(distribution);
        this.groupPay.set(groups);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  changeDimension(dim: Dimension): void {
    this.dimension.set(dim);
    this.api.by(dim).subscribe((groups) => this.groupPay.set(groups));
  }

  /** Groups in a sensible reading order: seniority for level, else by average pay. */
  orderedGroups(): PayGroup[] {
    const groups = this.groupPay()?.groups ?? [];
    if (this.dimension() === 'level') {
      const rank = new Map<string, number>(LEVELS.map((l, i) => [l, i]));
      return [...groups].sort((a, b) => (rank.get(a.key) ?? 99) - (rank.get(b.key) ?? 99));
    }
    return [...groups].sort((a, b) => b.averagePay - a.averagePay);
  }

  averagePayBars(): BarItem[] {
    return this.orderedGroups().map((g) => ({
      label: g.key.replace(/_/g, ' '),
      value: g.averagePay,
      hint: `${g.headcount.toLocaleString()} employees`,
    }));
  }

  distributionBars(): BarItem[] {
    const buckets = this.distribution()?.buckets ?? [];
    return buckets.map((b) => ({
      label: `${compact(b.from)} – ${compact(b.to)}`,
      value: b.count,
      hint: 'employees in this band (USD)',
    }));
  }
}

/** 125000 -> "$125k" for tight axis labels. */
export function compact(usd: number): string {
  if (usd >= 1_000_000) {
    return `$${(usd / 1_000_000).toFixed(1)}M`;
  }
  if (usd >= 1_000) {
    return `$${Math.round(usd / 1_000)}k`;
  }
  return `$${Math.round(usd)}`;
}
