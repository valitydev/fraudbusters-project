import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { PaymentReference } from '../../../api/fb-management/swagger-codegen/model/paymentReference';
import { ReferenceRow } from '../reference-table/reference-table.component';

@Component({
    selector: 'fb-payment-references-list',
    templateUrl: 'payment-references-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentReferencesListComponent {
    rows: ReferenceRow<PaymentReference>[] = [];

    @Input()
    set references(references: PaymentReference[]) {
        this.rows = references.map((source) => ({
            ...source,
            targetId: source.templateId,
            scopeId: source.shopId,
            source,
        }));
    }

    @Input() targetFragment?: string;

    @Output()
    deleteItem = new EventEmitter<PaymentReference>();

    @Input() tableLabel = 'References';
}
