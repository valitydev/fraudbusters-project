import { NgModule } from '@angular/core';

import { CreatePaymentReferenceComponent } from './components';
import { CreateGroupReferenceRoutingModule } from './create-group-reference-routing.module';
import { CreateGroupReferenceComponent } from './create-group-reference.component';
import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { ReferenceFormComponent } from '../../shared/components/reference-form/reference-form.component';

@NgModule({
    declarations: [CreateGroupReferenceComponent, CreatePaymentReferenceComponent],
    imports: [CreateGroupReferenceRoutingModule, ReferenceFormComponent],
    exports: [CreateGroupReferenceComponent],
    providers: [PaymentGroupsReferencesService, PaymentGroupsService],
})
export class CreateGroupReferenceModule {}
