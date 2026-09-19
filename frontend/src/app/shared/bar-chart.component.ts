import { Component, Input, computed, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';

export interface BarItem {
  label: string;
  value: number;
  /** Extra detail for the tooltip, e.g. "142 employees". */
  hint?: string;
}

/**
 * Horizontal single-series bar chart in plain HTML/CSS. One measure, one hue:
 * identity is carried by the row label, magnitude by bar length. Values are
 * shown as muted text in a column (table-like), hover reveals the hint.
 * A `<table>` fallback is rendered for screen readers.
 */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  imports: [DecimalPipe, MatTooltipModule],
  template: `
    <div class="chart" role="img" [attr.aria-label]="ariaLabel">
      @for (item of items; track item.label) {
        <div class="row" [matTooltip]="tooltip(item)" matTooltipPosition="above">
          <div class="label" [title]="item.label">{{ item.label }}</div>
          <div class="track">
            <div class="bar" [style.width.%]="width(item.value)"></div>
          </div>
          <div class="value">{{ valuePrefix }}{{ item.value | number:valueFormat }}</div>
        </div>
      }
    </div>

    <table class="sr-only">
      <caption>{{ ariaLabel }}</caption>
      <thead><tr><th>Label</th><th>Value</th></tr></thead>
      <tbody>
        @for (item of items; track item.label) {
          <tr><td>{{ item.label }}</td><td>{{ item.value | number:valueFormat }}</td></tr>
        }
      </tbody>
    </table>
  `,
  styles: `
    .chart { display: grid; gap: 6px; }
    .row {
      display: grid;
      grid-template-columns: minmax(90px, 170px) 1fr auto;
      align-items: center;
      gap: 12px;
      min-height: 22px;
    }
    .label {
      font-size: 13px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .track {
      height: 12px;
      background: var(--chart-track, #e8ebf0);
      border-radius: 4px;
      overflow: hidden;
    }
    .bar {
      height: 100%;
      background: var(--chart-primary, #005cbb);
      border-radius: 0 4px 4px 0;
      transition: width 200ms ease-out;
    }
    .value {
      font-size: 13px;
      font-variant-numeric: tabular-nums;
      color: var(--text-muted, #5f6368);
      min-width: 72px;
      text-align: right;
    }
    .sr-only {
      position: absolute; width: 1px; height: 1px; overflow: hidden;
      clip: rect(0 0 0 0); white-space: nowrap;
    }
  `,
})
export class BarChartComponent {
  @Input({ required: true }) set data(value: BarItem[]) {
    this.itemsSignal.set(value ?? []);
  }
  @Input() valueFormat = '1.0-0';
  @Input() valuePrefix = '';
  @Input() ariaLabel = 'Bar chart';

  private readonly itemsSignal = signal<BarItem[]>([]);
  private readonly max = computed(() =>
    this.itemsSignal().reduce((m, i) => Math.max(m, i.value), 0),
  );

  get items(): BarItem[] {
    return this.itemsSignal();
  }

  width(value: number): number {
    const max = this.max();
    return max > 0 ? Math.max(0, (value / max) * 100) : 0;
  }

  tooltip(item: BarItem): string {
    const v = `${this.valuePrefix}${new Intl.NumberFormat().format(Math.round(item.value))}`;
    return item.hint ? `${item.label}: ${v} · ${item.hint}` : `${item.label}: ${v}`;
  }
}
