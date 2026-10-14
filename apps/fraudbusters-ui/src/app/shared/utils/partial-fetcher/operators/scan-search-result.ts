import { Observable, of } from 'rxjs';
import { catchError, first, map, mergeScan } from 'rxjs/operators';

import { FetchAction } from '../fetch-action';
import { FetchFn } from '../fetch-fn';
import { FetchResult } from '../fetch-result';

export const handleFetchResultError =
    <R>(result: R[] = [], count?: number) =>
    (s: Observable<FetchResult<R>>): Observable<FetchResult<R>> =>
        s.pipe(
            catchError(() =>
                of<FetchResult<R>>({
                    result,
                    count,
                })
            )
        );

export const scanFetchResult =
    <P, R>(fn: FetchFn<P, R>) =>
    (s: Observable<FetchAction<P>>): Observable<FetchResult<R>> =>
        s.pipe(
            mergeScan<FetchAction<P>, FetchResult<R>>(
                ({ result, count }, { type, value }) => {
                    switch (type) {
                        case 'search':
                            return fn(value).pipe(first(), handleFetchResultError());
                        case 'fetchMore': {
                            const lastItem = result[result.length - 1];
                            // eslint-disable-next-line @typescript-eslint/no-explicit-any
                            return fn(value, (lastItem as any).id, lastItem).pipe(
                                first(),
                                map((r) => ({
                                    result: result.concat(r.result),
                                    count: r.count,
                                })),
                                handleFetchResultError(result, count)
                            );
                        }
                    }
                },
                { result: [] },
                1
            )
        );
