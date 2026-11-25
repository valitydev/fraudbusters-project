import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { Group } from '../../../api/fb-management/swagger-codegen/model/group';
import { PaymentGroupsService } from '../../../api/payments/groups';
import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

interface GroupFilters {
    searchValue?: string;
}

@Injectable()
export class FetchGroupsService extends PartialFetcher<Group, GroupFilters> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));

    constructor(private paymentGroupsService: PaymentGroupsService) {
        super(0);
    }

    protected fetch(params: GroupFilters): Observable<FetchResult<Group>> {
        return this.paymentGroupsService.filter(params.searchValue);
    }
}
