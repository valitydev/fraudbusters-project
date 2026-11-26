import { OperatorFunction } from 'rxjs';

import { FetchAction } from './fetch-action';
import { FetchFnContinuation } from './fetch-fn-continuation';
import { FetchResultContinuation } from './fetch-result-continuation';
import { scanFetchResultContinuation } from './operators/scan-continuation-search-result';
import { PagedFetcher } from './paged-fetcher';

/** Pages loaded by a server continuation token; "show more" while the server returns one. */
export abstract class PartialFetcherContinuation<R, P> extends PagedFetcher<R, P, FetchResultContinuation<R>> {
    protected constructor(debounceActionTime = 300) {
        super(debounceActionTime);
    }

    protected abstract fetch(...args: Parameters<FetchFnContinuation<P, R>>): ReturnType<FetchFnContinuation<P, R>>;

    protected scanPages(): OperatorFunction<FetchAction<P>, FetchResultContinuation<R>> {
        return scanFetchResultContinuation(this.fetch.bind(this) as FetchFnContinuation<P, R>);
    }

    protected emptyPage(): FetchResultContinuation<R> {
        return { result: [], continuationId: null };
    }

    protected hasMore({ continuationId }: FetchResultContinuation<R>): boolean {
        return !!continuationId;
    }
}
