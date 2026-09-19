import { toIsoDate } from './employee-form-dialog.component';

describe('toIsoDate', () => {
  it('formats a local date as yyyy-MM-dd with zero padding', () => {
    expect(toIsoDate(new Date(2024, 0, 5))).toBe('2024-01-05');
    expect(toIsoDate(new Date(2023, 11, 25))).toBe('2023-12-25');
  });
});
