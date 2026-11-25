import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { FraudUploaderRoutingModule } from './fraud-uploader-routing.module';
import { FraudUploaderComponent } from './fraud-uploader.component';
import { PaymentLoadDataService } from '../../../api/payments/load-data';

@NgModule({
    declarations: [FraudUploaderComponent],
    imports: [
        CommonModule,
        FraudUploaderRoutingModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        MatSnackBarModule,
    ],
    providers: [PaymentLoadDataService],
})
export class FraudUploaderListModule {}
