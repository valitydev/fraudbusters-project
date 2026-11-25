import { ClipboardModule } from '@angular/cdk/clipboard';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { SharedPipesModule } from '../../pipes';
import { ActionsComponent } from './components/actions/actions.component';
import { TemplateHeaderComponent } from './components/template-header/template-header.component';
import { TemplateItemComponent } from './components/template-item/template-item.component';
import { PaymentTemplatesListComponent } from './payment-templates-list.component';

@NgModule({
    imports: [
        CommonModule,
        ClipboardModule,
        MatIconModule,
        MatMenuModule,
        MatSnackBarModule,
        SharedPipesModule,
        MatButtonModule,
    ],
    declarations: [PaymentTemplatesListComponent, TemplateHeaderComponent, TemplateItemComponent, ActionsComponent],
    exports: [PaymentTemplatesListComponent],
})
export class PaymentTemplatesListModule {}
