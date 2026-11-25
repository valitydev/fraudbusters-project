import { EMPTY, Observable } from 'rxjs';
import { expand, map, reduce } from 'rxjs/operators';

export const CATALOG_SORT_OPTIONS = [
    { value: 'updated-desc', label: 'Last update (newest)' },
    { value: 'updated-asc', label: 'Last update (oldest)' },
    { value: 'id-asc', label: 'ID (A–Z)' },
    { value: 'id-desc', label: 'ID (Z–A)' },
] as const;

export type CatalogSort = (typeof CATALOG_SORT_OPTIONS)[number]['value'];

export function compareCatalogRows(
    leftId: string,
    rightId: string,
    leftDate: number | null,
    rightDate: number | null,
    sort: CatalogSort
): number {
    const ids = (leftId ?? '').localeCompare(rightId ?? '');
    if (sort === 'id-asc' || sort === 'id-desc') {
        return sort === 'id-asc' ? ids : -ids;
    }
    if (!Number.isFinite(leftDate) || !Number.isFinite(rightDate)) {
        return Number(Number.isFinite(rightDate)) - Number(Number.isFinite(leftDate)) || ids;
    }
    return (sort === 'updated-asc' ? leftDate - rightDate : rightDate - leftDate) || ids;
}

export function catalogTimestamp(value?: Date): number | null {
    if (!value) {
        return null;
    }
    const timestamp = new Date(value).getTime();
    return Number.isFinite(timestamp) ? timestamp : null;
}

/** Walk an ID-ordered endpoint's existing cursor contract before sorting the full list. */
export function loadCatalog<T extends { id?: string }>(
    fetchPage: (cursor?: string) => Observable<{ result: T[]; count: number }>
): Observable<T[]> {
    const page = (cursor?: string, loaded = 0) =>
        fetchPage(cursor).pipe(
            map(({ result, count }) => {
                const lastId = result[result.length - 1]?.id;
                const totalLoaded = loaded + result.length;
                return {
                    result,
                    loaded: totalLoaded,
                    next: totalLoaded < count && lastId !== cursor ? lastId : undefined,
                };
            })
        );

    return page().pipe(
        expand(({ next, loaded }) => (next ? page(next, loaded) : EMPTY)),
        reduce((rows, { result }) => {
            result.forEach((row) => rows.set(row.id, row));
            return rows;
        }, new Map<string, T>()),
        map((rows) => Array.from(rows.values()))
    );
}
