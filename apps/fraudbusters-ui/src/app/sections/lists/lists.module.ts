import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';

import { PaymentTemplatesModule } from '../../api';
import { PaymentTemplatesListModule } from '../../shared/components/templates-table';
import { TemplatesRoutingModule } from '../templates/templates-routing.module';
import { BlackListModule } from './components/black-list';
import { GreyListModule } from './components/grey-list';
import { WhiteListModule } from './components/white-list';
import { ListsRoutingModule } from './lists-routing.module';
import { ListsComponent } from './lists.component';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { SharedPipesModule } from '../../shared/pipes';

@NgModule({
    declarations: [ListsComponent],
    imports: [
        ListsRoutingModule,
        RouterModule,
        TemplatesRoutingModule,
        MatTabsModule,
        MatTooltipModule,
        CommonModule,
        MatButtonModule,
        MatIconModule,
        ConfirmActionDialogModule,
        SharedPipesModule,
        MatSnackBarModule,
        PaymentTemplatesModule,
        PaymentTemplatesListModule,
        WhiteListModule,
        BlackListModule,
        GreyListModule,
    ],
})
export class ListsModule {}
