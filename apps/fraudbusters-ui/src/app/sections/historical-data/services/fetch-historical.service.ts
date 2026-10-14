import { DatePipe } from '@angular/common';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { ConfigService } from '../../../config';
import { DateFormat } from '../../../shared/constants/date-format';
import { SortOrder } from '../../../shared/constants/sort-order';
import { FetchResultContinuation } from '../../../shared/utils/partial-fetcher/fetch-result-continuation';
import { PartialFetcherContinuation } from '../../../shared/utils/partial-fetcher/partial-fetcher-continuation';
import { SearchHistoricalParams } from '../search-historical-params';

export interface FetchPaymentParams {
    sortOrder?: SortOrder;
    size?: number;
    sortBy?: string;
    sortFieldValue?: string;
    from?: Date;
    to?: Date;
    paymentId?: string;
    cardToken?: string;
    shopId?: string;
    partyId?: string;
    status?: string;
    fingerprint?: string;
    email?: string;
    template?: string;
    rule?: string;
}

export abstract class FetchHistoricalService<T, P> extends PartialFetcherContinuation<T, FetchPaymentParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    protected pageSize = this.configService.pageSize;

    constructor(protected configService: ConfigService, protected datepipe: DatePipe) {
        super();
    }

    protected abstract filter(params: SearchHistoricalParams): Observable<P>;

    protected fetch(params: FetchPaymentParams, continuationId?: string): Observable<FetchResultContinuation<T>> {
        const {
            sortOrder,
            size,
            from,
            to,
            paymentId,
            cardToken,
            shopId,
            partyId,
            status,
            fingerprint,
            email,
            template,
            rule,
        } = params;
        return this.filter({
            from: this.datepipe.transform(from, DateFormat._yyyyMMDdHHMmSs),
            to: this.datepipe.transform(to, DateFormat._yyyyMMDdHHMmSs),
            sortOrder: sortOrder || SortOrder.Desc,
            paymentId: paymentId || '',
            cardToken: cardToken || '',
            shopId: shopId || '',
            partyId: partyId || '',
            status: status || '',
            fingerprint: fingerprint || '',
            email: email || '',
            template: template || '',
            rule: rule || '',
            size: size ? size : this.pageSize,
            ...(continuationId ? { continuationId } : {}),
        });
    }
}
