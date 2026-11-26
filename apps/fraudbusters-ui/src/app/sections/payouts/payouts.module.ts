import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

import { PayoutsService } from '../../api/payouts';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { SharedPipesModule } from '../../shared/pipes';
import { PayoutGroupReferenceFormComponent } from './components/payout-group-reference-form/payout-group-reference-form.component';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferenceFormComponent } from './components/payout-reference-form/payout-reference-form.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplateFormComponent } from './components/payout-template-form/payout-template-form.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { PayoutSectionComponent } from './payout-section.component';
import { PayoutsRoutingModule } from './payouts-routing.module';
import { PayoutsFeedbackService } from './services/payouts-feedback.service';
import { CatalogMetadataContentComponent } from '../../shared/components/catalog-panel/catalog-metadata-content.component';
import { CatalogPanelComponent } from '../../shared/components/catalog-panel/catalog-panel.component';
import { GroupsListModule } from '../../shared/components/groups-table';
import { ListRecordsTableComponent } from '../../shared/components/list-records-table/list-records-table.component';
import { ReferenceFormComponent } from '../../shared/components/reference-form/reference-form.component';
import { ReferenceTableComponent } from '../../shared/components/reference-table/reference-table.component';
import { TemplateEditorComponent } from '../../shared/components/template-editor/template-editor.component';
import { PaymentTemplatesListModule } from '../../shared/components/templates-table';

/** Payout configuration laid out as the payment sections of the same name. */

@NgModule({
    imports: [
        CatalogMetadataContentComponent,
        CatalogPanelComponent,
        GroupsListModule,
        ListRecordsTableComponent,
        ReferenceTableComponent,
        ReferenceFormComponent,
        PaymentTemplatesListModule,
        TemplateEditorComponent,
        PayoutsRoutingModule,
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        MatMenuModule,
        MatProgressBarModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTabsModule,
        ConfirmActionDialogModule,
        SharedPipesModule,
    ],
    declarations: [
        PayoutSectionComponent,
        PayoutTemplatesComponent,
        PayoutTemplateFormComponent,
        PayoutReferencesComponent,
        PayoutReferenceFormComponent,
        PayoutGroupsComponent,
        PayoutGroupReferencesComponent,
        PayoutGroupReferenceFormComponent,
        PayoutListsComponent,
    ],
    providers: [PayoutsService, PayoutsFeedbackService],
})
export class PayoutsModule {}
