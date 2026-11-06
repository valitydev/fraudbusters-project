import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchHistoricalWithdrawalsService } from '../../services/fetch-historical-withdrawals.service';

@Component({
    templateUrl: 'historical-withdrawals-data.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalWithdrawalsDataComponent {
    withdrawals$ = this.fetchWithdrawalsService.searchResult$;
    inProgress$ = this.fetchWithdrawalsService.inProgress$;
    hasMore$ = this.fetchWithdrawalsService.hasMore$;

    constructor(
        private fetchWithdrawalsService: FetchHistoricalWithdrawalsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(event) {
        this.fetchWithdrawalsService.search({
            partyId: event.partyId,
            walletId: event.walletId,
            withdrawalId: event.paymentId,
            status: event.status,
            cardToken: event.cardToken,
            fingerprint: event.fingerprint,
            customerId: event.customerId,
            userId: event.userId,
            email: event.email,
            terminal: event.terminal,
            providerCountry: event.country,
            from: event.from,
            to: event.to,
        });
    }

    fetchMore() {
        this.fetchWithdrawalsService.fetchMore();
    }
}
