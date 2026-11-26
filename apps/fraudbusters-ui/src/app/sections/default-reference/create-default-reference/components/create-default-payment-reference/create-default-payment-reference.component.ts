import { Component } from '@angular/core';

import { PaymentDefaultReferencesService } from '../../../../../api/payments/default-references';
import { PaymentTemplatesService } from '../../../../../api/payments/templates';
import {
    ReferenceFormConfig,
    toPaymentReference,
} from '../../../../../shared/components/reference-form/reference-draft';

@Component({
    template: '<fb-reference-form [config]="config"></fb-reference-form>',
    standalone: false,
})
export class CreateDefaultPaymentReferenceComponent {
    readonly config: ReferenceFormConfig = {
        title: 'New default references',
        scope: 'shop',
        backUrl: '/templates/default-references',
        loadTargetNames: (prefix) => this.templates.getTemplatesName(prefix),
        saveRow: (row) => this.references.save(toPaymentReference(row)),
    };

    constructor(
        private readonly references: PaymentDefaultReferencesService,
        private readonly templates: PaymentTemplatesService
    ) {}
}
