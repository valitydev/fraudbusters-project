import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataPayoutInspectResultsHeaderComponent } from './components/template-header/historical-data-payout-inspect-results-header.component';
import { HistoricalDataPayoutInspectResultsItemComponent } from './components/template-item/historical-data-payout-inspect-results-item.component';
import { HistoricalDataPayoutInspectResultsListComponent } from './historical-data-payout-inspect-results-list.component';
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
        ListHeaderModule,
        TagModule,
    ],
    declarations: [
        HistoricalDataPayoutInspectResultsListComponent,
        HistoricalDataPayoutInspectResultsHeaderComponent,
        HistoricalDataPayoutInspectResultsItemComponent,
    ],
    exports: [HistoricalDataPayoutInspectResultsListComponent],
})
export class HistoricalDataPayoutInspectResultsListModule {}
