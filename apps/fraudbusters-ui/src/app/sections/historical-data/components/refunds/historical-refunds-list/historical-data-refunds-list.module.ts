import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataRefundsHeaderComponent } from './components/template-header/historical-data-refunds-header.component';
import { HistoricalDataRefundsItemComponent } from './components/template-item/historical-data-refunds-item.component';
import { RefundStatusToColorPipe } from './components/template-item/refund-status-to-color.pipe';
import { HistoricalDataRefundsListComponent } from './historical-data-refunds-list.component';
import { ListHeaderModule } from '../../../../../shared/components/list-header';
import { TagModule } from '../../../../../shared/components/tag';
import { SharedPipesModule } from '../../../../../shared/pipes';

@NgModule({
    imports: [
        CommonModule,
        FlexModule,
        MatExpansionModule,
        MatDividerModule,
        SharedPipesModule,
        MatButtonModule,
        ListHeaderModule,
        TagModule,
    ],
    declarations: [
        HistoricalDataRefundsListComponent,
        HistoricalDataRefundsHeaderComponent,
        HistoricalDataRefundsItemComponent,
        RefundStatusToColorPipe,
    ],
    exports: [HistoricalDataRefundsListComponent],
})
export class HistoricalDataRefundsListModule {}
