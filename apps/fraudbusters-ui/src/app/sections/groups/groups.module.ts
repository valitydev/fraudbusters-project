import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

import { GroupReferencesComponent } from './components/group-references/group-references.component';
import { PaymentGroupsComponent } from './components/payments-groups/payment-groups.component';
import { GroupsRoutingModule } from './groups-routing.module';
import { GroupsComponent } from './groups.component';
import { GroupsReferenceService } from './services/groups-reference/groups-reference.service';
import { DataSetService } from '../../api/payments/data-set';
import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { CatalogMetadataContentComponent } from '../../shared/components/catalog-panel/catalog-metadata-content.component';
import { CatalogPanelComponent } from '../../shared/components/catalog-panel/catalog-panel.component';
import { ConfirmActionDialogModule } from '../../shared/components/confirm-action-dialog';
import { GroupReferencesListModule } from './components/group-references/components/group-references-list';
import { GroupsListModule } from '../../shared/components/groups-table';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';
import { SearchFieldService } from '../../shared/services/utils/search-field.service';

@NgModule({
    declarations: [GroupsComponent, PaymentGroupsComponent, GroupReferencesComponent],
    imports: [
        CatalogMetadataContentComponent,
        CatalogPanelComponent,
        ConfirmActionDialogModule,
        MatButtonModule,
        CommonModule,
        MatProgressBarModule,
        MatSelectModule,
        MatTabsModule,
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        MatNativeDateModule,
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
