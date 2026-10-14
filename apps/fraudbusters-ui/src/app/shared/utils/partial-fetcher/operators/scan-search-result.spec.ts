import { of, Subject } from 'rxjs';

import { FetchAction } from '../fetch-action';
import { FetchFn } from '../fetch-fn';
import { scanFetchResult } from './scan-search-result';

interface Item {
    id: string;
    insertTime: string;
}

describe('scanFetchResult', () => {
    it('passes the last id and item when fetching the next page', () => {
        const params = { searchValue: 'value' };
        const firstPage = [{ id: 'first-id', insertTime: '2026-08-21T10:00:00' }];
        const secondPage = [{ id: 'second-id', insertTime: '2026-08-21T10:01:00' }];
        const calls: Parameters<FetchFn<typeof params, Item>>[] = [];
        const fetch: FetchFn<typeof params, Item> = (...args) => {
            calls.push(args);
            return of({ result: calls.length === 1 ? firstPage : secondPage, count: 2 });
        };
        const actions$ = new Subject<FetchAction<typeof params>>();

        actions$.pipe(scanFetchResult(fetch)).subscribe();
        actions$.next({ type: 'search', value: params });
        actions$.next({ type: 'fetchMore', value: params });

        expect(calls[1]).toEqual([params, firstPage[0].id, firstPage[0]]);
    });
});
