import { DestroyRef } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { BehaviorSubject, NEVER, Observable, of } from 'rxjs';

import { FetchWbListService } from './services/fetch-wb-list.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListComponent } from './wb-list.component';
import { PaymentListsService } from '../../../api/payments/lists';
import { ListType } from '../../constants/list-type';

describe('WbListComponent', () => {
    const create = (names: Observable<string[] | null>) => {
        // The fetcher reports "loading" until its first response, like PartialFetcher.
        const fetcherBusy = new BehaviorSubject(true);
        const search = jasmine.createSpy('search').and.callFake(() => fetcherBusy.next(false));
        const fetcher = {
            inProgress$: fetcherBusy,
            fetchResultChanges$: NEVER,
            search,
        } as unknown as FetchWbListService;
        const lists = { getNames: () => names } as unknown as PaymentListsService;
        const route = { queryParamMap: of(convertToParamMap({})) } as unknown as ActivatedRoute;
        const remover = { removed$: NEVER } as unknown as RemoveWbListComponentService;
        const destroyRef = { onDestroy: () => () => undefined } as unknown as DestroyRef;
        const component = new WbListComponent(route, fetcher, remover, lists, {} as MatSnackBar, destroyRef);
        component.listType = ListType.Black;
        const busy: boolean[] = [];
        component.inProgress$.subscribe((value) => busy.push(value));
        component.ngOnInit();
        component.listNames$.subscribe();
        return { busy, search };
    };

    it('stops loading when the list type has no lists, without asking the backend', () => {
        const { busy, search } = create(of([]));
        expect(search).not.toHaveBeenCalled();
        expect(busy[busy.length - 1]).toBeFalse();
    });

    it('treats a names response without the list as empty', () => {
        const { busy, search } = create(of(null));
        expect(search).not.toHaveBeenCalled();
        expect(busy[busy.length - 1]).toBeFalse();
    });

    it('searches the lists of the type when there are some', () => {
        const { search } = create(of(['EMAIL', 'CARD']));
        expect(search).toHaveBeenCalledWith(jasmine.objectContaining({ listNames: ['EMAIL', 'CARD'] }));
    });
});
