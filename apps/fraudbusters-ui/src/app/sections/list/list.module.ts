import { NgModule } from '@angular/core';

import { ListRoutingModule } from './list-routing.module';
import { PaymentListsModule } from '../../api/payments/lists';

@NgModule({
    imports: [ListRoutingModule, PaymentListsModule],
})
export class ListModule {}
