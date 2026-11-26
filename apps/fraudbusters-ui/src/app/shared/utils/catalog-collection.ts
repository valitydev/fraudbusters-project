import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, startWith, switchMap, tap } from 'rxjs/operators';

import { CatalogSort } from './catalog';

/** The catalog search text from the URL (`?searchQuery=`, written by fb-catalog-search), trimmed. */
export function catalogSearchQuery(route: ActivatedRoute): Observable<string> {
    return route.queryParamMap.pipe(
        map((params) => (params.get('searchQuery') ?? '').trim()),
        distinctUntilChanged()
    );
}

interface CatalogCollectionView<T> {
    rows: T[];
    total: number;
    count: number;
    sort: CatalogSort;
    busy: boolean;
    failed: boolean;
}

export class CatalogCollection<T> {
    readonly view$: Observable<CatalogCollectionView<T>>;

    private readonly reload$ = new BehaviorSubject<void>(undefined);
    private readonly sort$ = new BehaviorSubject<CatalogSort>('updated-desc');
    private readonly visible$: BehaviorSubject<number>;

    constructor(
        route: ActivatedRoute,
        private pageSize: number,
        load: (query: string) => Observable<T[]>,
        compare: (left: T, right: T, sort: CatalogSort) => number,
        matches?: (row: T, query: string) => boolean
    ) {
        this.visible$ = new BehaviorSubject(pageSize);
        const query$ = catalogSearchQuery(route).pipe(tap(() => this.visible$.next(pageSize)));
        const data$ = combineLatest([matches ? of('') : query$, this.reload$]).pipe(
            switchMap(([query]) =>
                load(query).pipe(
                    map((rows) => ({ rows, busy: false, failed: false })),
                    startWith({ rows: [] as T[], busy: true, failed: false }),
                    catchError(() => of({ rows: [] as T[], busy: false, failed: true }))
                )
            )
        );

        this.view$ = combineLatest([data$, this.sort$, this.visible$, matches ? query$ : of('')]).pipe(
            map(([data, sort, visible, query]) => {
                const term = query.toLowerCase();
                const rows = term && matches ? data.rows.filter((row) => matches(row, term)) : [...data.rows];
                rows.sort((left, right) => compare(left, right, sort));
                return {
                    ...data,
                    total: data.rows.length,
                    count: rows.length,
                    rows: rows.slice(0, visible),
                    sort,
                };
            }),
            shareReplay({ bufferSize: 1, refCount: true })
        );
    }

    sort(value: CatalogSort): void {
        this.visible$.next(this.pageSize);
        this.sort$.next(value);
    }

    more(): void {
        this.visible$.next(this.visible$.value + this.pageSize);
    }

    refresh(): void {
        this.reload$.next();
    }
}
