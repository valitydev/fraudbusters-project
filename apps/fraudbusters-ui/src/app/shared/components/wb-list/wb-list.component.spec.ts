import { DestroyRef } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { NEVER, of, Subject } from 'rxjs';

import { FetchWbListService } from './services/fetch-wb-list.service';
import { ListsEventsService } from './services/lists-events.service';
import { RemoveWbListComponentService } from './services/remove-wb-list.service';
import { WbListComponent } from './wb-list.component';
import { ListType } from '../../constants/list-type';
import { CatalogMetadataService } from '../catalog-panel/catalog-metadata.service';

describe(WbListComponent.name, () => {
    const create = () => {
        const search = jasmine.createSpy('search');
        const fetcher = { inProgress$: NEVER, fetchResultChanges$: NEVER, search } as unknown as FetchWbListService;
        const names = new Subject<string[]>();
        const metadata = { listNames: () => names } as unknown as CatalogMetadataService;
        const route = { queryParamMap: of(convertToParamMap({})) } as unknown as ActivatedRoute;
        const remover = { removed$: NEVER } as unknown as RemoveWbListComponentService;
        const destroyRef = { onDestroy: () => () => undefined } as unknown as DestroyRef;
        const component = new WbListComponent(
            route,
            fetcher,
            remover,
            {} as MatSnackBar,
            destroyRef,
            new ListsEventsService(),
            metadata
        );
        component.listType = ListType.Black;
        component.ngOnInit();
        return { component, names, search };
    };

    it('starts an unfiltered list request before the names arrive', () => {
        const { component, names, search } = create();
        expect(search).toHaveBeenCalledOnceWith(jasmine.objectContaining({ listType: ListType.Black }));
        expect(search.calls.mostRecent().args[0].listNames).toBeUndefined();

        const options = jasmine.createSpy('options');
        component.listNames$.subscribe(options);
        names.next(['EMAIL', 'CARD']);
        expect(options).toHaveBeenCalledOnceWith(['EMAIL', 'CARD']);
        expect(search).toHaveBeenCalledTimes(1);
    });

    it('sends the selected list name as a filter', () => {
        const { component, search } = create();
        component.setListName('EMAIL');
        expect(search.calls.mostRecent().args[0].listNames).toEqual(['EMAIL']);
    });
});
