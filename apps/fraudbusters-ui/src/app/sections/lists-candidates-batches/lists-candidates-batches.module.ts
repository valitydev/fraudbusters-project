import { NgModule } from '@angular/core';

import { ListsCandidatesBatchesRoutingModule } from './lists-candidates-batches-routing.module';
import { PaymentListsService } from '../../api/payments/lists';

@NgModule({
    imports: [ListsCandidatesBatchesRoutingModule],
    providers: [PaymentListsService],
})
export class ListsCandidatesBatchesModule {}
