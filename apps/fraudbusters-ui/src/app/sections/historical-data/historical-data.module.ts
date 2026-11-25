import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterModule } from '@angular/router';

import { HistoricalDataRoutingModule } from './historical-data-routing.module';
import { HistoricalDataComponent } from './historical-data.component';
import { DataSetService } from '../../api/payments/data-set';
import { HistoricalDataService } from '../../api/payments/historical-data';
import { PaymentListsService } from '../../api/payments/lists';
import { PayoutsService } from '../../api/payouts';

@NgModule({
    declarations: [HistoricalDataComponent],
    imports: [CommonModule, RouterModule, MatTabsModule, HistoricalDataRoutingModule],
    providers: [HistoricalDataService, PayoutsService, PaymentListsService, DataSetService, DatePipe],
})
export class HistoricalDataModule {}
