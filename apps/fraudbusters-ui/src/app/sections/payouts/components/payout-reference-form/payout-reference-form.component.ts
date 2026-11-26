import { Component } from '@angular/core';

import { PayoutsService } from '../../../../api/payouts';
import { ReferenceFormConfig } from '../../../../shared/components/reference-form/reference-draft';
import { toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    template: '<fb-reference-form [config]="config"></fb-reference-form>',
    standalone: false,
})
export class PayoutReferenceFormComponent {
    readonly config: ReferenceFormConfig = {
        title: 'New references',
        scope: 'wallet',
        backUrl: '/payouts/templates/references',
        loadTargetNames: (prefix) => this.payouts.getTemplateNames(prefix),
        saveBatch: (rows) =>
            this.payouts.saveReferences(
                rows.map(({ targetId, partyId, scopeId }) => ({
                    templateId: targetId,
                    isGlobal: false,
                    ...toPayoutScope(partyId, scopeId),
                }))
            ),
        onSaved: () => this.feedback.done('References created'),
    };

    constructor(private readonly payouts: PayoutsService, private readonly feedback: PayoutsFeedbackService) {}
}
