import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchHistoricalPayoutsService } from '../../services/fetch-historical-payouts.service';

@Component({
    templateUrl: 'historical-payouts-data.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalPayoutsDataComponent {
    payouts$ = this.fetchPayoutsService.searchResult$;
    inProgress$ = this.fetchPayoutsService.inProgress$;
    hasMore$ = this.fetchPayoutsService.hasMore$;

    constructor(
        private fetchPayoutsService: FetchHistoricalPayoutsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(event) {
        this.fetchPayoutsService.search({
            partyId: event.partyId,
            walletId: event.walletId,
            payoutId: event.paymentId,
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
        this.fetchPayoutsService.fetchMore();
    }
}
