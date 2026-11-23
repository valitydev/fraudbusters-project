import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';

import { LAYOUT_GAP_M } from '../../../../tokens';
import { FetchHistoricalPayoutInspectResultsService } from '../../services/fetch-historical-payout-inspect-results.service';

@Component({
    templateUrl: 'historical-payout-inspect-results-data.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalPayoutInspectResultsDataComponent {
    inspectResults$ = this.fetchPayoutInspectResultsService.searchResult$;
    inProgress$ = this.fetchPayoutInspectResultsService.inProgress$;
    hasMore$ = this.fetchPayoutInspectResultsService.hasMore$;

    constructor(
        private fetchPayoutInspectResultsService: FetchHistoricalPayoutInspectResultsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    search(event) {
        this.fetchPayoutInspectResultsService.search({
            partyId: event.partyId,
            walletId: event.walletId,
            payoutId: event.paymentId,
            status: event.status,
            cardToken: event.cardToken,
            fingerprint: event.fingerprint,
            customerId: event.customerId,
            userId: event.userId,
            email: event.email,
            template: event.template,
            rule: event.rule,
            from: event.from,
            to: event.to,
        });
    }

    fetchMore() {
        this.fetchPayoutInspectResultsService.fetchMore();
    }
}
