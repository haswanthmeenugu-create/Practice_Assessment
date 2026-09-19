import { ReplaceUnderscorePipe } from './replace-underscore.pipe';

describe('ReplaceUnderscorePipe', () => {
  const pipe = new ReplaceUnderscorePipe();

  it('replaces every underscore with a space', () => {
    expect(pipe.transform('HUMAN_RESOURCES')).toBe('HUMAN RESOURCES');
    expect(pipe.transform('ON_LEAVE')).toBe('ON LEAVE');
  });

  it('handles null and undefined', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });
});
