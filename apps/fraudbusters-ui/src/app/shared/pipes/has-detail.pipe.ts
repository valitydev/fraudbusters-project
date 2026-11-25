import { Pipe, PipeTransform } from '@angular/core';

/** Whether a row detail is worth showing: not empty, blank or a "-" placeholder. Keeps 0 and false. */
export function hasDetail(value: unknown): boolean {
    if (value === null || value === undefined) {
        return false;
    }
    const text = String(value).trim();
    return text !== '' && text !== '-' && text !== '—';
}

@Pipe({
    name: 'hasDetail',
    standalone: true,
})
export class HasDetailPipe implements PipeTransform {
    transform(value: unknown): boolean {
        return hasDetail(value);
    }
}
