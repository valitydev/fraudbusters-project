import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataChargebackHeaderComponent } from './components/template-header/historical-data-chargeback-header.component';
import { ChargebackStatusToColorPipe } from './components/template-item/chargeback-status-to-color.pipe';
import { HistoricalDataChargebackItemComponent } from './components/template-item/historical-data-chargeback-item.component';
import { HistoricalDataChargebackListComponent } from './historical-data-chargeback-list.component';
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
        MatCheckboxModule,
    ],
    declarations: [
        HistoricalDataChargebackListComponent,
        HistoricalDataChargebackHeaderComponent,
        HistoricalDataChargebackItemComponent,
        ChargebackStatusToColorPipe,
    ],
    exports: [HistoricalDataChargebackListComponent],
})
export class HistoricalDataChargebackListModule {}
