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
import { FetchFnContinuation } from './fetch-fn-continuation';
import { FetchResultContinuation } from './fetch-result-continuation';
import { scanAction } from './operators';
import { progress } from '../../operators';
import { scanFetchResultContinuation } from './operators/scan-continuation-search-result';

export abstract class PartialFetcherContinuation<R, P> {
    fetchResultChanges$: Observable<{ result: R[]; hasMore: boolean; continuationId: string }>;

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
                return of({ result: [], continuationId: null });
            })
        );

        this.fetchResultChanges$ = fetchResultWithErrorHandling$.pipe(
            map(({ result, continuationId }) => ({
                result,
                continuationId,
                hasMore: !!continuationId,
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

    protected getFetchResult(actionWithParams$: Observable<FetchAction<P>>): Observable<FetchResultContinuation<R>> {
        const fetchFn = this.fetch.bind(this) as FetchFnContinuation<P, R>;
        return actionWithParams$.pipe(scanFetchResultContinuation(fetchFn), shareReplay(1));
    }

    private getActionWithParams(debounceActionTime: number): Observable<FetchAction<P>> {
        return this.action$.pipe(scanAction, debounceActionTime ? debounceTime(debounceActionTime) : tap(), share());
    }
}
