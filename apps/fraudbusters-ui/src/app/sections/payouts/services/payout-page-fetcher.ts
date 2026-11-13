import { Observable } from 'rxjs';
import { distinctUntilChanged, shareReplay } from 'rxjs/operators';

import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

export const PAYOUT_PAGE_SIZE = 100;

/** Pages a payout list as the payment lists are paged: the next page continues after the last row's id. */
export class PayoutPageFetcher<R, P> extends PartialFetcher<R, P> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));

    constructor(private fetchPage: (params: P, lastId?: string) => Observable<FetchResult<R>>) {
        super();
    }

    protected fetch(params: P, lastId?: string): Observable<FetchResult<R>> {
        return this.fetchPage(params, lastId);
    }
}
