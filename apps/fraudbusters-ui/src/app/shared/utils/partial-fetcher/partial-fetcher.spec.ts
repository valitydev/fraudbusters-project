import { fakeAsync, tick } from '@angular/core/testing';
import { Observable, of } from 'rxjs';

import { FetchResult } from './fetch-result';
import { PartialFetcher } from './partial-fetcher';

class CountingFetcher extends PartialFetcher<{ id: string }, string> {
    calls = 0;

    constructor() {
        super();
    }

    protected fetch(): Observable<FetchResult<{ id: string }>> {
        this.calls += 1;
        return of({ result: [{ id: 'a' }], count: 1 });
    }
}

describe('PartialFetcher', () => {
    it('asks the backend once per search', fakeAsync(() => {
        const fetcher = new CountingFetcher();
        fetcher.search('value');
        tick(300);
        expect(fetcher.calls).toBe(1);
    }));

    it('reports loading from the start, while a search runs, and not after a synchronous response', fakeAsync(() => {
        const fetcher = new CountingFetcher();
        const busy: boolean[] = [];
        fetcher.doAction$.subscribe((value) => busy.push(value));
        expect(busy).toEqual([true]);
        fetcher.search('value');
        tick(300);
        expect(busy[busy.length - 1]).toBeFalse();
    }));

    it('treats a page without the list as empty', fakeAsync(() => {
        class EmptyFetcher extends PartialFetcher<{ id: string }, string> {
            constructor() {
                super();
            }

            protected fetch(): Observable<FetchResult<{ id: string }>> {
                return of({ result: null, count: 0 });
            }
        }
        const fetcher = new EmptyFetcher();
        const pages: { id: string }[][] = [];
        fetcher.searchResult$.subscribe((rows) => pages.push(rows));
        fetcher.search('value');
        tick(300);
        expect(pages).toEqual([[]]);
    }));
});
