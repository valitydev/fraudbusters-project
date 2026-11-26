import { OperatorFunction } from 'rxjs';

import { FetchAction } from './fetch-action';
import { FetchFn } from './fetch-fn';
import { FetchResult } from './fetch-result';
import { scanFetchResult } from './operators';
import { PagedFetcher } from './paged-fetcher';

/** Pages loaded by the last row (id and item); "show more" while fewer rows than `count` are loaded. */
export abstract class PartialFetcher<R, P> extends PagedFetcher<R, P, FetchResult<R>> {
    protected constructor(debounceActionTime = 300) {
        super(debounceActionTime);
    }

    protected abstract fetch(...args: Parameters<FetchFn<P, R>>): ReturnType<FetchFn<P, R>>;

    protected scanPages(): OperatorFunction<FetchAction<P>, FetchResult<R>> {
        return scanFetchResult(this.fetch.bind(this) as FetchFn<P, R>);
    }

    protected emptyPage(): FetchResult<R> {
        return { result: [], count: 0 };
    }

    protected hasMore({ result, count }: FetchResult<R>): boolean {
        return (result ?? []).length < count;
    }
}
