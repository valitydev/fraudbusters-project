import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

import { PaymentTemplatesModule } from '../../api';
import { PaymentTemplatesComponent } from './components/payment-templates/payment-templates.component';
import { FetchDefaultReferencesService } from './services/fetch-default-references.service';
import { TemplatesRoutingModule } from './templates-routing.module';
import { TemplatesComponent } from './templates.component';
import { PaymentDefaultReferencesService } from '../../api/payments/default-references';
import { PaymentEmulateService } from '../../api/payments/emulate';
import { PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { PaymentReferencesService } from '../../api/payments/references';
import { CatalogPanelComponent } from '../../shared/components/catalog-panel/catalog-panel.component';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { PaymentReferencesListModule } from '../../shared/components/payment-references-list';
import { SharedPipesModule } from '../../shared/pipes';
import { DefaultReferenceModule } from '../default-reference';
import { ReferenceModule } from '../reference';
import { DefaultPaymentReferencesComponent } from './components/payment-dafeult-references/default-payment-references.component';
import { PaymentReferencesComponent } from './components/payment-references/payment-references.component';
import { PaymentTemplatesListModule } from '../../shared/components/templates-table';

@NgModule({
    imports: [
        CatalogPanelComponent,
        TemplatesRoutingModule,
        MatTabsModule,
        CommonModule,
        MatButtonModule,
        MatIconModule,
        ConfirmActionDialogModule,
        SharedPipesModule,
        MatSnackBarModule,
        MatProgressBarModule,
        PaymentTemplatesModule,
        PaymentTemplatesListModule,
        DefaultReferenceModule,
        ReferenceModule,
        PaymentReferencesListModule,
    ],
    declarations: [
        TemplatesComponent,
        PaymentTemplatesComponent,
        PaymentReferencesComponent,
        DefaultPaymentReferencesComponent,
    ],
    providers: [
        PaymentReferencesService,
        PaymentDefaultReferencesService,
        PaymentGroupsReferencesService,
        PaymentEmulateService,
        FetchDefaultReferencesService,
    ],
})
export class TemplatesModule {}
