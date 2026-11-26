import { Component } from '@angular/core';
import { map } from 'rxjs/operators';

import { PaymentGroupsService } from '../../../../api/payments/groups';
import { PaymentGroupsReferencesService } from '../../../../api/payments/groups-references';
import {
    ReferenceFormConfig,
    toPaymentGroupReference,
} from '../../../../shared/components/reference-form/reference-draft';

@Component({
    template: '<fb-reference-form [config]="config"></fb-reference-form>',
    standalone: false,
})
export class CreatePaymentReferenceComponent {
    readonly config: ReferenceFormConfig = {
        title: 'New group references',
        target: 'group',
        scope: 'shop',
        backUrl: '/groups/references',
        loadTargetNames: (prefix) =>
            this.groups.filter(prefix).pipe(map((response) => response.result.map((group) => group.groupId))),
        saveBatch: (rows) => this.references.saveGroupReference(rows.map(toPaymentGroupReference)),
    };

    constructor(
        private readonly references: PaymentGroupsReferencesService,
        private readonly groups: PaymentGroupsService
    ) {}
}
