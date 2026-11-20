import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataWithdrawalInspectResultsHeaderComponent } from './components/template-header/historical-data-withdrawal-inspect-results-header.component';
import { HistoricalDataWithdrawalInspectResultsItemComponent } from './components/template-item/historical-data-withdrawal-inspect-results-item.component';
import { HistoricalDataWithdrawalInspectResultsListComponent } from './historical-data-withdrawal-inspect-results-list.component';
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
        HistoricalDataWithdrawalInspectResultsListComponent,
        HistoricalDataWithdrawalInspectResultsHeaderComponent,
        HistoricalDataWithdrawalInspectResultsItemComponent,
    ],
    exports: [HistoricalDataWithdrawalInspectResultsListComponent],
})
export class HistoricalDataWithdrawalInspectResultsListModule {}
