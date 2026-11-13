import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import {
    NgxMatDatetimePickerModule,
    NgxMatNativeDateModule,
    NgxMatTimepickerModule,
} from '@angular-material-components/datetime-picker';

import { PayoutsService } from '../../api/payouts';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { EmptySearchResultModule } from '../../shared/components/empty-search-result';
import { HeadlineModule } from '../../shared/components/headline';
import { ListHeaderModule } from '../../shared/components/list-header';
import { ShowMorePanelModule } from '../../shared/components/show-more-panel';
import { SharedPipesModule } from '../../shared/pipes';
import { PayoutGroupFormComponent } from './components/payout-group-form/payout-group-form.component';
import { PayoutGroupReferenceFormComponent } from './components/payout-group-reference-form/payout-group-reference-form.component';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListRowsFormComponent } from './components/payout-list-rows-form/payout-list-rows-form.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferenceFormComponent } from './components/payout-reference-form/payout-reference-form.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplateFormComponent } from './components/payout-template-form/payout-template-form.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { PayoutSectionComponent } from './payout-section.component';
import { PayoutsRoutingModule } from './payouts-routing.module';
import { PayoutsFeedbackService } from './services/payouts-feedback.service';

/** Payout configuration laid out as the payment sections of the same name. */
@NgModule({
    imports: [
        PayoutsRoutingModule,
        CommonModule,
        FlexLayoutModule,
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatCardModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatDialogModule,
        MatDividerModule,
        MatExpansionModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatMenuModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTabsModule,
        NgxMatDatetimePickerModule,
        NgxMatNativeDateModule,
        NgxMatTimepickerModule,
        ConfirmActionDialogModule,
        EmptySearchResultModule,
        HeadlineModule,
        ListHeaderModule,
        ShowMorePanelModule,
        SharedPipesModule,
    ],
    declarations: [
        PayoutSectionComponent,
        PayoutTemplatesComponent,
        PayoutTemplateFormComponent,
        PayoutReferencesComponent,
        PayoutReferenceFormComponent,
        PayoutGroupsComponent,
        PayoutGroupFormComponent,
        PayoutGroupReferencesComponent,
        PayoutGroupReferenceFormComponent,
        PayoutListsComponent,
        PayoutListRowsFormComponent,
    ],
    providers: [PayoutsService, PayoutsFeedbackService],
})
export class PayoutsModule {}
