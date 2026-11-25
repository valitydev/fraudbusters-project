import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { PaymentTemplatesModule } from '../../api';
import { CopyTemplateComponent } from './copy-template/copy-template.component';
import { CreateTemplateComponent } from './create-template/create-template.component';
import { EditTemplateComponent } from './edit-template/edit-template.component';
import { TemplatesService } from './services/templates/templates.service';
import { TemplateRoutingModule } from './template-routing.module';
import { TemplateComponent } from './template.component';
import { CreateTemplateModule } from '../../shared/components/create-template/create-template.module';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';

@NgModule({
    declarations: [TemplateComponent, EditTemplateComponent, CreateTemplateComponent, CopyTemplateComponent],
    imports: [
        MatSnackBarModule,
        TemplateRoutingModule,
        MatButtonModule,
        PaymentTemplatesModule,
        CreateTemplateModule,
        MatIconModule,
        CommonModule,
    ],
    providers: [TemplatesService, ErrorHandlerService],
})
export class TemplateModule {}
