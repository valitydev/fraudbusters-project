import { merge, Observable, of, Subject } from 'rxjs';
import {
    catchError,
    debounceTime,
    distinctUntilChanged,
    filter,
    map,
    share,
    shareReplay,
    startWith,
    tap,
} from 'rxjs/operators';

import { FetchAction } from './fetch-action';
import { FetchFn } from './fetch-fn';
import { FetchResult } from './fetch-result';
import { scanAction, scanFetchResult } from './operators';
import { progress } from '../../operators';

export abstract class PartialFetcher<R, P> {
    fetchResultChanges$: Observable<{ result: R[]; hasMore: boolean; count: number }>;

    readonly searchResult$: Observable<R[]>;
    readonly hasMore$: Observable<boolean>;
    readonly doAction$: Observable<boolean>;
    readonly doSearchAction$: Observable<boolean>;
    readonly errors$ = new Subject();
    private action$ = new Subject<FetchAction<P>>();

    constructor(debounceActionTime: number = 300) {
        const actionWithParams$ = this.getActionWithParams(debounceActionTime);
        const fetchResult$ = this.getFetchResult(actionWithParams$);

        // Handle errors after progress operators have processed them
        const fetchResultWithErrorHandling$ = fetchResult$.pipe(
            catchError((error) => {
                this.errors$.next(error);
                console.error('Partial fetcher error: ', error);
                return of({ result: [], count: 0 });
            })
        );

        this.fetchResultChanges$ = fetchResultWithErrorHandling$.pipe(
            map(({ result, count }) => ({
                result,
                count,
                hasMore: result.length < count,
            })),
            share()
        );
        this.searchResult$ = this.fetchResultChanges$.pipe(
            map((changes) => changes.result),
            shareReplay(1)
        );

        this.hasMore$ = this.fetchResultChanges$.pipe(
            map((changes) => changes.hasMore),
            startWith(null as boolean),
            distinctUntilChanged(),
            shareReplay(1)
        );

        this.doAction$ = progress(actionWithParams$, fetchResultWithErrorHandling$, true).pipe(shareReplay(1));
        this.doSearchAction$ = progress(
            actionWithParams$.pipe(filter(({ type }) => type === 'search')),
            fetchResultWithErrorHandling$,
            true
        ).pipe(shareReplay(1));

        merge(
            this.searchResult$,
            this.hasMore$,
            this.doAction$,
            this.doSearchAction$,
            this.errors$,
            this.fetchResultChanges$
        ).subscribe();
    }

    search(value: P) {
        this.action$.next({ type: 'search', value });
    }

    refresh() {
        this.action$.next({ type: 'search' });
    }

    fetchMore(value?: P) {
        this.action$.next(value ? { type: 'fetchMore', value } : { type: 'fetchMore' });
    }

    protected abstract fetch(...args: Parameters<FetchFn<P, R>>): ReturnType<FetchFn<P, R>>;

    protected getFetchResult(actionWithParams$: Observable<FetchAction<P>>): Observable<FetchResult<R>> {
        const fetchFn = this.fetch.bind(this) as FetchFn<P, R>;
        return actionWithParams$.pipe(scanFetchResult(fetchFn));
    }

    private getActionWithParams(debounceActionTime: number): Observable<FetchAction<P>> {
        return this.action$.pipe(scanAction, debounceActionTime ? debounceTime(debounceActionTime) : tap());
    }
}
