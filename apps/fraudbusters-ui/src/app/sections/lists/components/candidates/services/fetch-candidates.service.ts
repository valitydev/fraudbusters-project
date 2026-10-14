import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { WbListCandidateBatch } from '../../../../../api/fb-management/swagger-codegen/model/wbListCandidateBatch';
import { PaymentListsService } from '../../../../../api/payments/lists';
import { SearchParams } from '../../../../../shared/model/search-params';
import { FetchResult, PartialFetcher } from '../../../../../shared/utils/partial-fetcher';

@Injectable()
export class FetchCandidatesService extends PartialFetcher<WbListCandidateBatch, SearchParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));

    constructor(private paymentListsService: PaymentListsService) {
        super();
    }

    fetch(params: SearchParams): Observable<FetchResult<WbListCandidateBatch>> {
        return this.paymentListsService.findCandidatesBatch(params);
    }
}
