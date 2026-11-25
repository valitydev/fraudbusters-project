import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { PaymentDataSetsListComponent } from './payment-data-sets-list.component';

@NgModule({
    imports: [CommonModule, MatButtonModule, MatIconModule, MatMenuModule],
    declarations: [PaymentDataSetsListComponent],
    exports: [PaymentDataSetsListComponent],
})
export class PaymentDataSetsListModule {}
