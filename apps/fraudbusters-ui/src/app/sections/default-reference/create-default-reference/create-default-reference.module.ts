import { NgModule } from '@angular/core';

import { CreateDefaultPaymentReferenceComponent } from './components';
import { CreateDefaultReferenceRoutingModule } from './create-default-reference-routing.module';
import { CreateDefaultReferenceComponent } from './create-default-reference.component';
import { PaymentDefaultReferencesModule } from '../../../api/payments/default-references';
import { PaymentTemplatesModule } from '../../../api/payments/templates';
import { ReferenceFormComponent } from '../../../shared/components/reference-form/reference-form.component';

@NgModule({
    declarations: [CreateDefaultReferenceComponent, CreateDefaultPaymentReferenceComponent],
    imports: [
        CreateDefaultReferenceRoutingModule,
        PaymentDefaultReferencesModule,
        PaymentTemplatesModule,
        ReferenceFormComponent,
    ],
    exports: [CreateDefaultReferenceComponent],
})
export class CreateDefaultReferenceModule {}
