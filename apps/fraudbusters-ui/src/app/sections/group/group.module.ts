import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';

import { PaymentTemplatesModule } from '../../api';
import { CreateGroupComponent } from './create-group/create-group.component';
import { EditGroupComponent } from './edit-group/edit-group.component';
import { GroupRoutingModule } from './group-routing.module';
import { GroupComponent } from './group.component';
import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesModule, PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { CreateGroupModule } from '../../shared/components/create-group/create-group.module';
import { HeadlineModule } from '../../shared/components/headline';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';
import { CreateGroupReferenceModule } from '../create-group-reference';
import { TemplatesService } from '../template/services/templates/templates.service';

@NgModule({
    imports: [
        GroupRoutingModule,
        MatToolbarModule,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatTableModule,
        CommonModule,
        MatSortModule,
        FormsModule,
        MatAutocompleteModule,
        MatIconModule,
        MatSnackBarModule,
        PaymentTemplatesModule,
        CreateGroupReferenceModule,
        PaymentGroupsReferencesModule,
        FlexModule,
        HeadlineModule,
        CreateGroupModule,
    ],
    declarations: [GroupComponent, EditGroupComponent, CreateGroupComponent],
    providers: [PaymentGroupsService, TemplatesService, ErrorHandlerService, PaymentGroupsReferencesService],
})
export class GroupModule {}
