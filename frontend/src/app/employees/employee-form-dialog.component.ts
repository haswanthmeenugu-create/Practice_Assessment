import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EmployeeApi } from '../core/employee-api.service';
import {
  CURRENCIES, DEPARTMENTS, EMPLOYMENT_TYPES, Employee, EmployeeRequest, LEVELS, STATUSES,
} from '../core/models';

export interface EmployeeFormData {
  employee?: Employee;
}

/**
 * Create / edit an employee. Returns the saved employee on close, or
 * undefined when cancelled. Validation mirrors the backend constraints so
 * most mistakes are caught before a round-trip.
 */
@Component({
  selector: 'app-employee-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatDatepickerModule, MatNativeDateModule,
    MatProgressBarModule,
  ],
  templateUrl: './employee-form-dialog.component.html',
  styleUrl: './employee-form-dialog.component.scss',
})
export class EmployeeFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(EmployeeApi);
  private readonly ref = inject(MatDialogRef<EmployeeFormDialogComponent, Employee | undefined>);
  readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);

  readonly departments = DEPARTMENTS;
  readonly levels = LEVELS;
  readonly currencies = CURRENCIES;
  readonly employmentTypes = EMPLOYMENT_TYPES;
  readonly statuses = STATUSES;

  saving = false;

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    country: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
    department: ['ENGINEERING' as Employee['department'], Validators.required],
    jobTitle: ['', [Validators.required, Validators.maxLength(120)]],
    level: ['MID' as Employee['level'], Validators.required],
    baseSalary: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['USD' as Employee['currency'], Validators.required],
    employmentType: ['FULL_TIME' as Employee['employmentType'], Validators.required],
    status: ['ACTIVE' as Employee['status'], Validators.required],
    hireDate: [new Date(), Validators.required],
  });

  constructor() {
    const e = this.data.employee;
    if (e) {
      this.form.patchValue({ ...e, hireDate: new Date(e.hireDate) });
    }
  }

  get isEdit(): boolean {
    return !!this.data.employee;
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const v = this.form.getRawValue();
    const request: EmployeeRequest = {
      ...v,
      country: v.country.toUpperCase(),
      hireDate: toIsoDate(v.hireDate),
    };
    const call = this.isEdit
      ? this.api.update(this.data.employee!.id, request)
      : this.api.create(request);

    call.subscribe({
      next: (saved) => this.ref.close(saved),
      error: () => (this.saving = false), // interceptor already showed the message
    });
  }

  cancel(): void {
    this.ref.close(undefined);
  }
}

/** Format a Date as yyyy-MM-dd in local time (what the API expects). */
export function toIsoDate(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
