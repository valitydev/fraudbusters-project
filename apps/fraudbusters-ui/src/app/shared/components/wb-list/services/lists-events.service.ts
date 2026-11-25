import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { ListType } from '../../../constants/list-type';

/**
 * Connects the Lists page header (counts, Clean by CSV) with the list tab under it:
 * whoever changes a list reports it here, the others reload.
 */
@Injectable()
export class ListsEventsService {
    readonly changed$ = new Subject<ListType>();

    changed(listType: ListType): void {
        this.changed$.next(listType);
    }
}
