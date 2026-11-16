import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { HistoricalDataWithdrawalsHeaderComponent } from './components/template-header/historical-data-withdrawals-header.component';
import { HistoricalDataWithdrawalsItemComponent } from './components/template-item/historical-data-withdrawals-item.component';
import { WithdrawalStatusToColorPipe } from './components/template-item/withdrawal-status-to-color.pipe';
import { HistoricalDataWithdrawalsListComponent } from './historical-data-withdrawals-list.component';
import { ListHeaderModule } from '../../../../../shared/components/list-header';
import { TagModule } from '../../../../../shared/components/tag';

@NgModule({
    imports: [CommonModule, FlexModule, MatExpansionModule, MatDividerModule, ListHeaderModule, TagModule],
    declarations: [
        HistoricalDataWithdrawalsListComponent,
        HistoricalDataWithdrawalsHeaderComponent,
        HistoricalDataWithdrawalsItemComponent,
        WithdrawalStatusToColorPipe,
    ],
    exports: [HistoricalDataWithdrawalsListComponent],
})
export class HistoricalDataWithdrawalsListModule {}
