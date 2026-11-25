import { NgModule } from '@angular/core';

import { CreatePaymentReferenceComponent } from './components';
import { CreateReferenceRoutingModule } from './create-reference-routing.module';
import { CreateReferenceComponent } from './create-reference.component';
import { PaymentReferencesModule } from '../../../api/payments/references';
import { PaymentTemplatesModule } from '../../../api/payments/templates';
import { ReferenceFormComponent } from '../../../shared/components/reference-form/reference-form.component';

@NgModule({
    declarations: [CreateReferenceComponent, CreatePaymentReferenceComponent],
    imports: [CreateReferenceRoutingModule, ReferenceFormComponent, PaymentReferencesModule, PaymentTemplatesModule],
    exports: [CreateReferenceComponent],
})
export class CreateReferenceModule {}
