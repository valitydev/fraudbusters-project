import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';

import { PayoutsService } from '../../api/payouts';
import { PayoutGroupReferencesComponent } from './components/payout-group-references/payout-group-references.component';
import { PayoutGroupsComponent } from './components/payout-groups/payout-groups.component';
import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutReferencesComponent } from './components/payout-references/payout-references.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { PayoutsRoutingModule } from './payouts-routing.module';
import { PayoutsComponent } from './payouts.component';
import { PayoutsFeedbackService } from './services/payouts-feedback.service';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { EmptySearchResultModule } from '../../shared/components/empty-search-result';

@NgModule({
    imports: [
        PayoutsRoutingModule,
        CommonModule,
        FlexModule,
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatButtonToggleModule,
        MatCardModule,
        MatCheckboxModule,
        MatDialogModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTableModule,
        MatTabsModule,
        ConfirmActionDialogModule,
        EmptySearchResultModule,
    ],
    declarations: [
        PayoutsComponent,
        PayoutTemplatesComponent,
        PayoutReferencesComponent,
        PayoutGroupsComponent,
        PayoutGroupReferencesComponent,
        PayoutListsComponent,
    ],
    providers: [PayoutsService, PayoutsFeedbackService],
})
export class PayoutsModule {}
