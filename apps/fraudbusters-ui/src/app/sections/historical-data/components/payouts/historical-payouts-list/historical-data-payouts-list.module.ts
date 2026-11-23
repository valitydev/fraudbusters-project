import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataPayoutsHeaderComponent } from './components/template-header/historical-data-payouts-header.component';
import { HistoricalDataPayoutsItemComponent } from './components/template-item/historical-data-payouts-item.component';
import { PayoutStatusToColorPipe } from './components/template-item/payout-status-to-color.pipe';
import { HistoricalDataPayoutsListComponent } from './historical-data-payouts-list.component';
import { ListHeaderModule } from '../../../../../shared/components/list-header';
import { TagModule } from '../../../../../shared/components/tag';

@NgModule({
    imports: [CommonModule, FlexModule, MatExpansionModule, MatDividerModule, ListHeaderModule, TagModule],
    declarations: [
        HistoricalDataPayoutsListComponent,
        HistoricalDataPayoutsHeaderComponent,
        HistoricalDataPayoutsItemComponent,
        PayoutStatusToColorPipe,
    ],
    exports: [HistoricalDataPayoutsListComponent],
})
export class HistoricalDataPayoutsListModule {}
