import { Component } from '@angular/core';

import { PaymentReferencesService } from '../../../../../api/payments/references';
import { PaymentTemplatesService } from '../../../../../api/payments/templates';
import {
    ReferenceFormConfig,
    toPaymentReference,
} from '../../../../../shared/components/reference-form/reference-draft';

@Component({
    template: '<fb-reference-form [config]="config"></fb-reference-form>',
    standalone: false,
})
export class CreatePaymentReferenceComponent {
    readonly config: ReferenceFormConfig = {
        title: 'New references',
        scope: 'shop',
        backUrl: '/templates/references',
        loadTargetNames: (prefix) => this.templates.getTemplatesName(prefix),
        saveBatch: (rows) => this.references.saveReferences(rows.map(toPaymentReference)),
    };

    constructor(
        private readonly references: PaymentReferencesService,
        private readonly templates: PaymentTemplatesService
    ) {}
}
