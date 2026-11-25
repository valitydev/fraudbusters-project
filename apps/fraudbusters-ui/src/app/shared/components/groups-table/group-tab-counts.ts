import { Injectable } from '@angular/core';
import { combineLatest, Observable, of, Subject } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

interface CountResponse {
    count?: number;
}

@Injectable()
export class GroupTabCounts {
    private readonly refreshed = new Subject<void>();

    load(
        groups: () => Observable<CountResponse>,
        references: () => Observable<CountResponse>
    ): Observable<{ list: number | null; references: number | null }> {
        const count = (fetch: () => Observable<CountResponse>) =>
            this.refreshed.pipe(
                startWith(undefined),
                switchMap(() =>
                    fetch().pipe(
                        map(({ count }) => count ?? null),
                        catchError(() => of(null))
                    )
                ),
                startWith(null)
            );

        return combineLatest({ list: count(groups), references: count(references) }).pipe(
            shareReplay({ bufferSize: 1, refCount: true })
        );
    }

    refresh(): void {
        this.refreshed.next();
    }
}
