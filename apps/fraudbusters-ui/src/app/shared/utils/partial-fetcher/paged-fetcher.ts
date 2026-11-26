import { BehaviorSubject, merge, Observable, of, OperatorFunction, Subject } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, share, shareReplay, tap } from 'rxjs/operators';

import { FetchAction } from './fetch-action';
import { scanAction } from './operators';

/** A loaded page: rows plus whatever the backend needs to load the next one. */
export interface PagedResult<R> {
    result?: R[];
}

/**
 * Search and "show more" over a paged backend list. Every action asks the backend once: all
 * outputs share one request stream, and the busy flags are set inside it, before and after
 * each request, so they never miss a fast (or synchronous) response.
 */
export abstract class PagedFetcher<R, P, Page extends PagedResult<R>> {
    readonly fetchResultChanges$: Observable<Page & { result: R[]; hasMore: boolean }>;
    readonly searchResult$: Observable<R[]>;
    readonly hasMore$: Observable<boolean | null>;
    /** True from the start until the first page, and while any action loads. */
    readonly doAction$: Observable<boolean>;
    /** Like doAction$, but "show more" does not raise it. */
    readonly doSearchAction$: Observable<boolean>;
    readonly errors$ = new Subject<unknown>();

    private readonly action$ = new Subject<FetchAction<P>>();
    private readonly busy$ = new BehaviorSubject(true);
    private readonly searchBusy$ = new BehaviorSubject(true);

    protected constructor(debounceActionTime = 300) {
        const actions$ = this.action$.pipe(
            scanAction,
            debounceActionTime ? debounceTime(debounceActionTime) : tap(),
            tap(({ type }) => {
                this.busy$.next(true);
                if (type === 'search') {
                    this.searchBusy$.next(true);
                }
            })
        );

        this.fetchResultChanges$ = actions$.pipe(
            this.scanPages(),
            catchError((error) => {
                this.errors$.next(error);
                console.error('Partial fetcher error: ', error);
                return of(this.emptyPage());
            }),
            // An empty page may come without the list (result: null), which is not an error.
            map((page) => ({ ...page, result: page.result ?? [], hasMore: this.hasMore(page) })),
            tap(() => {
                this.busy$.next(false);
                this.searchBusy$.next(false);
            }),
            share()
        );
        this.searchResult$ = this.fetchResultChanges$.pipe(
            map(({ result }) => result),
            shareReplay(1)
        );
        this.hasMore$ = merge(of(null), this.fetchResultChanges$.pipe(map(({ hasMore }) => hasMore))).pipe(
            distinctUntilChanged(),
            shareReplay(1)
        );
        this.doAction$ = this.busy$.pipe(distinctUntilChanged(), shareReplay(1));
        this.doSearchAction$ = this.searchBusy$.pipe(distinctUntilChanged(), shareReplay(1));

        // Keep the pipeline running even before the view subscribes.
        merge(this.searchResult$, this.hasMore$, this.doAction$, this.doSearchAction$).subscribe();
    }

    search(value: P): void {
        this.action$.next({ type: 'search', value });
    }

    refresh(): void {
        this.action$.next({ type: 'search' });
    }

    fetchMore(value?: P): void {
        this.action$.next(value ? { type: 'fetchMore', value } : { type: 'fetchMore' });
    }

    /** Turns search and "show more" actions into the accumulated pages. */
    protected abstract scanPages(): OperatorFunction<FetchAction<P>, Page>;

    protected abstract emptyPage(): Page;

    protected abstract hasMore(page: Page): boolean;
}
