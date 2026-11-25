import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { PaymentTemplatesService } from '../../../api';
import { TagModule } from '../tag';
import { ResultStatusToColorPipe } from './components/data-set-payment-item/result-status-to-color.pipe';
import { TestingDataSetListComponent } from './testing-data-set-list.component';
import { HasDetailPipe } from '../../pipes/has-detail.pipe';
import { TemplateService } from '../create-template/services/template/template.service';

@NgModule({
    imports: [HasDetailPipe, CommonModule, MatButtonModule, MatIconModule, TagModule],
    declarations: [TestingDataSetListComponent, ResultStatusToColorPipe],
    exports: [TestingDataSetListComponent],
    providers: [TemplateService, PaymentTemplatesService],
})
export class TestingDataSetListModule {}
