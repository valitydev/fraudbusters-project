import { Component } from '@angular/core';
import { defer } from 'rxjs';
import { map } from 'rxjs/operators';

import { PayoutsService } from '../../../../api/payouts';
import { ReferenceFormConfig } from '../../../../shared/components/reference-form/reference-draft';
import { toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

/** Binds payout template groups to a party or wallet; without a party the group applies to every payout. */
@Component({
    template: '<fb-reference-form [config]="config"></fb-reference-form>',
})
export class PayoutGroupReferenceFormComponent {
    readonly config: ReferenceFormConfig = {
        title: 'New group references',
        target: 'group',
        scope: 'wallet',
        partyOptional: true,
        backUrl: '/payouts/groups/references',
        loadTargetNames: (prefix) =>
            this.payouts.findGroups(prefix).pipe(map((response) => response.result.map((group) => group.groupId))),
        // The API saves references per group, so rows go one by one.
        saveRow: ({ targetId, partyId, scopeId }) =>
            defer(() =>
                this.payouts.saveGroupReferences(targetId, [{ groupId: targetId, ...toPayoutScope(partyId, scopeId) }])
            ),
        onSaved: () => this.feedback.done('Group references created'),
    };

    constructor(private readonly payouts: PayoutsService, private readonly feedback: PayoutsFeedbackService) {}
}
