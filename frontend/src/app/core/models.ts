/**
 * Types mirroring the backend API contract. Kept as string unions so the
 * enum values round-trip through JSON unchanged.
 */

export const DEPARTMENTS = [
  'ENGINEERING', 'PRODUCT', 'DATA', 'SALES', 'MARKETING',
  'FINANCE', 'HUMAN_RESOURCES', 'OPERATIONS', 'CUSTOMER_SUPPORT', 'LEGAL',
] as const;
export type Department = typeof DEPARTMENTS[number];

export const LEVELS = [
  'INTERN', 'JUNIOR', 'MID', 'SENIOR', 'STAFF', 'PRINCIPAL', 'MANAGER', 'DIRECTOR', 'VP',
] as const;
export type Level = typeof LEVELS[number];

export const CURRENCIES = [
  'USD', 'EUR', 'GBP', 'INR', 'CAD', 'AUD', 'SGD', 'JPY', 'BRL', 'ZAR',
] as const;
export type Currency = typeof CURRENCIES[number];

export const EMPLOYMENT_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT'] as const;
export type EmploymentType = typeof EMPLOYMENT_TYPES[number];

export const STATUSES = ['ACTIVE', 'ON_LEAVE', 'TERMINATED'] as const;
export type EmployeeStatus = typeof STATUSES[number];

/** Countries present in the seed data; the form also accepts any 2-letter code. */
export const COUNTRIES = ['US', 'GB', 'DE', 'FR', 'IE', 'CA', 'AU', 'SG', 'JP', 'IN', 'BR', 'ZA'] as const;

export interface Employee {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  country: string;
  department: Department;
  jobTitle: string;
  level: Level;
  baseSalary: number;
  currency: Currency;
  baseSalaryUsd: number;
  employmentType: EmploymentType;
  status: EmployeeStatus;
  hireDate: string; // ISO yyyy-MM-dd
}

export type EmployeeRequest = Omit<Employee, 'id' | 'employeeCode' | 'baseSalaryUsd'>;

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeSearch {
  q?: string;
  country?: string;
  department?: Department | '';
  level?: Level | '';
  status?: EmployeeStatus | '';
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
}

export interface Overview {
  baseCurrency: string;
  activeHeadcount: number;
  totalPayroll: number;
  averageSalary: number;
  medianSalary: number;
  minSalary: number;
  maxSalary: number;
}

export type Dimension = 'country' | 'department' | 'level';

export interface PayGroup {
  key: string;
  headcount: number;
  totalPay: number;
  averagePay: number;
  minPay: number;
  maxPay: number;
}

export interface GroupPay {
  dimension: string;
  baseCurrency: string;
  groups: PayGroup[];
}

export interface DistributionBucket {
  from: number;
  to: number;
  count: number;
}

export interface Distribution {
  baseCurrency: string;
  buckets: DistributionBucket[];
}

/** Error body produced by the backend's GlobalExceptionHandler. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string | { field: string; message: string }[];
}
