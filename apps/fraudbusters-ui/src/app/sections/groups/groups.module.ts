import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
    NgxMatDatetimePickerModule,
    NgxMatNativeDateModule,
    NgxMatTimepickerModule,
} from '@angular-material-components/datetime-picker';

import { GroupReferencesComponent } from './components/group-references/group-references.component';
import { PaymentGroupsComponent } from './components/payments-groups/payment-groups.component';
import { GroupsRoutingModule } from './groups-routing.module';
import { GroupsComponent } from './groups.component';
import { GroupsReferenceService } from './services/groups-reference/groups-reference.service';
import { DataSetService } from '../../api/payments/data-set';
import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { EmptySearchResultModule } from '../../shared/components/empty-search-result';
import { ShowMorePanelModule } from '../../shared/components/show-more-panel';
import { ShowMoreContinuationPanelModule } from '../../shared/components/show-more-panel-continuation';
import { GroupReferencesListModule } from './components/group-references/components/group-references-list';
import { SearchFieldService } from '../../shared/services/utils/search-field.service';
import { ReferencesSearchComponent } from './components/group-references/components/references-search/references-search.component';
import { GroupsListModule } from './components/payments-groups/components/groups-list';
import { GroupsSearchComponent } from './components/payments-groups/components/groups-search/groups-search.component';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';

@NgModule({
    declarations: [
        GroupsComponent,
        PaymentGroupsComponent,
        GroupReferencesComponent,
        ReferencesSearchComponent,
        GroupsSearchComponent,
    ],
    imports: [
        FlexModule,
        MatButtonModule,
        MatCardModule,
        CommonModule,
        MatProgressBarModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatTabsModule,
        CommonModule,
        MatCardModule,
        MatTableModule,
        MatSortModule,
        MatButtonModule,
        MatIconModule,
        MatToolbarModule,
        MatMenuModule,
        EmptySearchResultModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        MatDividerModule,
        ShowMorePanelModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatTooltipModule,
        NgxMatTimepickerModule,
        NgxMatNativeDateModule,
        NgxMatDatetimePickerModule,
        ShowMoreContinuationPanelModule,
        GroupsRoutingModule,
        GroupsListModule,
        GroupReferencesListModule,
    ],
    providers: [
        DatePipe,
        DataSetService,
        ErrorHandlerService,
        PaymentGroupsService,
        SearchFieldService,
        GroupsReferenceService,
        PaymentGroupsReferencesService,
    ],
})
export class GroupsModule {}
