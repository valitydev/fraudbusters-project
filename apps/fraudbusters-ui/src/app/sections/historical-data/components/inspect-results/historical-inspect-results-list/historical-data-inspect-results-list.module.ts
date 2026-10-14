import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataInspectResultsHeaderComponent } from './components/template-header/historical-data-inspect-results-header.component';
import { HistoricalDataInspectResultsItemComponent } from './components/template-item/historical-data-inspect-results-item.component';
import { HistoricalDataInspectResultsListComponent } from './historical-data-inspect-results-list.component';
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
        HistoricalDataInspectResultsListComponent,
        HistoricalDataInspectResultsHeaderComponent,
        HistoricalDataInspectResultsItemComponent,
    ],
    exports: [HistoricalDataInspectResultsListComponent],
})
export class HistoricalDataInspectResultsListModule {}
