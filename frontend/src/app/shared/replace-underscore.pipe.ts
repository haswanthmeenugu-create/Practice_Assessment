import { Pipe, PipeTransform } from '@angular/core';

/** "Human_Resources" -> "Human Resources". For displaying enum names. */
@Pipe({ name: 'replaceUnderscore', standalone: true })
export class ReplaceUnderscorePipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return (value ?? '').replace(/_/g, ' ');
  }
}
