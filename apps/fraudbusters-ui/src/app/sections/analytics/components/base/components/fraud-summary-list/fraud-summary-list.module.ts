import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { FraudSummaryHeaderComponent } from './components/fraud-summary-header/fraud-summary-header.component';
import { FraudSummaryItemComponent } from './components/fraud-summary-item/fraud-summary-item.component';
import { FraudSummaryListComponent } from './fraud-summary-list.component';

@NgModule({
    imports: [CommonModule],
    declarations: [FraudSummaryListComponent, FraudSummaryHeaderComponent, FraudSummaryItemComponent],
    exports: [FraudSummaryListComponent],
})
export class FraudSummaryListModule {}
