import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { TestingDataSetListComponent } from './testing-data-set-list.component';
import { PaymentTemplatesService } from '../../../api';
import { TemplateService } from '../create-template/services/template/template.service';
import { HistoricalTableComponent } from '../historical-table/historical-table.component';

@NgModule({
    imports: [CommonModule, HistoricalTableComponent],
    declarations: [TestingDataSetListComponent],
    exports: [TestingDataSetListComponent],
    providers: [TemplateService, PaymentTemplatesService],
})
export class TestingDataSetListModule {}
