import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { EmulationTemplateRoutingModule } from './emulation-template-routing.module';
import { EmulationTemplateComponent } from './emulation-template.component';
import { PaymentEmulateModule } from '../../../../../api/payments/emulate';
import { SharedPipesModule } from '../../../../../shared/pipes';
import { ErrorHandlerService } from '../../../../../shared/services/utils/error-handler.service';
import { EmulationTemplateService } from '../../../services/emulation/emulation-template.service';

@NgModule({
    declarations: [EmulationTemplateComponent],
    imports: [
        CommonModule,
        EmulationTemplateRoutingModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
        MatProgressBarModule,
        FormsModule,
        SharedPipesModule,
        PaymentEmulateModule,
    ],
    providers: [EmulationTemplateService, ErrorHandlerService],
})
export class EmulationTemplateModule {}
