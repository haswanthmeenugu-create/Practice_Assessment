import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe, DatePipe, DecimalPipe, LowerCasePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { EmployeeApi } from '../core/employee-api.service';
import {
  COUNTRIES, DEPARTMENTS, Employee, EmployeeSearch, LEVELS, STATUSES,
} from '../core/models';
import { EmployeeFormDialogComponent, EmployeeFormData } from './employee-form-dialog.component';
import { ConfirmDialogComponent } from '../shared/confirm-dialog.component';

/**
 * Paginated, filterable employee table. All paging/sorting/filtering is done
 * server-side; the component only holds the current page.
 */
@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    ReactiveFormsModule, CurrencyPipe, DatePipe, DecimalPipe, LowerCasePipe,
    MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule,
    MatDialogModule, MatProgressBarModule, MatTooltipModule,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent {
  private readonly api = inject(EmployeeApi);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly departments = DEPARTMENTS;
  readonly levels = LEVELS;
  readonly statuses = STATUSES;
  readonly countries = COUNTRIES;

  readonly columns = [
    'employeeCode', 'name', 'country', 'department', 'jobTitle', 'level',
    'baseSalary', 'baseSalaryUsd', 'status', 'hireDate', 'actions',
  ];

  readonly filters = this.fb.nonNullable.group({
    q: '',
    country: '',
    department: '' as EmployeeSearch['department'],
    level: '' as EmployeeSearch['level'],
    status: '' as EmployeeSearch['status'],
  });

  readonly rows = signal<Employee[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);

  page = 0;
  size = 20;
  sort = 'lastName';
  direction: 'asc' | 'desc' = 'asc';

  constructor() {
    this.filters.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page = 0;
        this.load();
      });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const f = this.filters.getRawValue();
    const criteria: EmployeeSearch = {
      ...f,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction,
    };
    this.api.search(criteria).subscribe({
      next: (res) => {
        this.rows.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onPage(e: PageEvent): void {
    this.page = e.pageIndex;
    this.size = e.pageSize;
    this.load();
  }

  onSort(s: Sort): void {
    if (!s.direction) {
      this.sort = 'lastName';
      this.direction = 'asc';
    } else {
      this.sort = s.active === 'name' ? 'lastName' : s.active;
      this.direction = s.direction;
    }
    this.page = 0;
    this.load();
  }

  clearFilters(): void {
    this.filters.reset();
  }

  hasActiveFilters(): boolean {
    const f = this.filters.getRawValue();
    return !!(f.q || f.country || f.department || f.level || f.status);
  }

  add(): void {
    this.openForm({});
  }

  edit(employee: Employee): void {
    this.openForm({ employee });
  }

  remove(employee: Employee): void {
    this.dialog
      .open(ConfirmDialogComponent, {
        data: {
          title: 'Delete employee',
          message: `Delete ${employee.firstName} ${employee.lastName} (${employee.employeeCode})? This cannot be undone.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.api.delete(employee.id).subscribe(() => {
          this.snackBar.open('Employee deleted', undefined, { duration: 3000 });
          this.load();
        });
      });
  }

  private openForm(data: EmployeeFormData): void {
    this.dialog
      .open<EmployeeFormDialogComponent, EmployeeFormData, Employee | undefined>(
        EmployeeFormDialogComponent, { data, width: '640px', disableClose: true },
      )
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.snackBar.open(
            data.employee ? 'Employee updated' : `Created ${saved.employeeCode}`,
            undefined, { duration: 3000 },
          );
          this.load();
        }
      });
  }
}
