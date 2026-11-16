import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { distinctUntilChanged, shareReplay } from 'rxjs/operators';

import { Withdrawal } from '../../../api/fb-management/swagger-codegen/model/withdrawal';
import { PayoutsService } from '../../../api/payouts';
import { ConfigService } from '../../../config';
import { DateFormat } from '../../../shared/constants/date-format';
import { SortOrder } from '../../../shared/constants/sort-order';
import { FetchResultContinuation } from '../../../shared/utils/partial-fetcher/fetch-result-continuation';
import { PartialFetcherContinuation } from '../../../shared/utils/partial-fetcher/partial-fetcher-continuation';

export interface FetchWithdrawalParams {
    from?: Date | string;
    to?: Date | string;
    partyId?: string;
    walletId?: string;
    withdrawalId?: string;
    status?: string;
    cardToken?: string;
    fingerprint?: string;
    customerId?: string;
    userId?: string;
    email?: string;
    terminal?: string;
    providerCountry?: string;
}

@Injectable()
export class FetchHistoricalWithdrawalsService extends PartialFetcherContinuation<Withdrawal, FetchWithdrawalParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));

    constructor(
        private payoutsService: PayoutsService,
        private configService: ConfigService,
        private datepipe: DatePipe
    ) {
        super();
    }

    protected fetch(
        { from, to, ...filters }: FetchWithdrawalParams,
        continuationId?: string
    ): Observable<FetchResultContinuation<Withdrawal>> {
        const filled = Object.fromEntries(Object.entries(filters).filter(([, value]) => !!value));
        return this.payoutsService.filterWithdrawals({
            ...filled,
            from: this.datepipe.transform(from, DateFormat._yyyyMMDdHHMmSs),
            to: this.datepipe.transform(to, DateFormat._yyyyMMDdHHMmSs),
            sortOrder: SortOrder.Desc,
            size: this.configService.pageSize,
            ...(continuationId ? { continuationId } : {}),
        });
    }
}
