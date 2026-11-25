import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { DataSetRoutingModule } from './data-set-routing.module';
import { DataSetComponent } from './data-set.component';
import { TestingDataSetComponent } from './testing/testing-data-set.component';
import { DataSetService } from '../../api/payments/data-set';
import { TestingDataSetListModule } from '../../shared/components/testing-data-set-list';
import { DataSetTemplateModule } from '../../shared/components/testing-data-set-list/components/data-set-template/data-set-template.module';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';

@NgModule({
    declarations: [DataSetComponent, TestingDataSetComponent],
    imports: [
        CommonModule,
        DataSetRoutingModule,
        MatButtonModule,
        MatIconModule,
        TestingDataSetListModule,
        DataSetTemplateModule,
    ],
    providers: [DataSetService, ErrorHandlerService],
})
export class DataSetModule {}
