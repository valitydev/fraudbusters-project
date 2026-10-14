import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { WbListRecord } from '../../../../../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { PaymentListsService } from '../../../../../../../api/payments/lists';
import { SearchParams } from '../../../../../../../shared/model/search-params';
import { FetchResult, PartialFetcher } from '../../../../../../../shared/utils/partial-fetcher';

@Injectable()
export class FetchCandidatesRecordService extends PartialFetcher<WbListRecord, SearchParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));

    constructor(private paymentListsService: PaymentListsService) {
        super();
    }

    fetch(params: SearchParams): Observable<FetchResult<WbListRecord>> {
        return this.paymentListsService.findCandidates(params);
    }
}
