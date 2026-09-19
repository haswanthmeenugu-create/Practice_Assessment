import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · ACME Salary',
    loadComponent: () =>
      import('./employees/employee-list.component').then((m) => m.EmployeeListComponent),
  },
  {
    path: 'analytics',
    title: 'Pay analytics · ACME Salary',
    loadComponent: () =>
      import('./analytics/analytics-dashboard.component').then((m) => m.AnalyticsDashboardComponent),
  },
  { path: '**', redirectTo: 'employees' },
];
