import { NgModule } from '@angular/core';

import { PaymentReferencesListComponent } from './payment-references-list.component';
import { ReferenceTableComponent } from '../reference-table/reference-table.component';

@NgModule({
    imports: [ReferenceTableComponent],
    declarations: [PaymentReferencesListComponent],
    exports: [PaymentReferencesListComponent],
})
export class PaymentReferencesListModule {}
