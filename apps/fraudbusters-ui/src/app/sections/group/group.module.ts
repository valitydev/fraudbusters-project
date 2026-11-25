import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { GroupRoutingModule } from './group-routing.module';
import { GroupComponent } from './group.component';
import { PaymentTemplatesModule } from '../../api';
import { PaymentGroupFormComponent } from './payment-group-form/payment-group-form.component';
import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesModule, PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { GroupEditorComponent } from '../../shared/components/group-editor/group-editor.component';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';
import { CreateGroupReferenceModule } from '../create-group-reference';
import { TemplatesService } from '../template/services/templates/templates.service';

@NgModule({
    imports: [
        GroupRoutingModule,
        MatButtonModule,
        CommonModule,
        FormsModule,
        MatAutocompleteModule,
        MatIconModule,
        MatSnackBarModule,
        PaymentTemplatesModule,
        CreateGroupReferenceModule,
        PaymentGroupsReferencesModule,
        GroupEditorComponent,
    ],
    declarations: [GroupComponent, PaymentGroupFormComponent],
    providers: [PaymentGroupsService, TemplatesService, ErrorHandlerService, PaymentGroupsReferencesService],
})
export class GroupModule {}
