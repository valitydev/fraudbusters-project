import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { CandidatesBatchesTableComponent } from './candidates-batches-table.component';
import { ActionsComponent } from './components/actions/actions.component';
import { CandidatesBatchesTableHeaderComponent } from './components/candidates-batches-table-header/candidates-batches-table-header.component';
import { CandidatesBatchesTableItemComponent } from './components/candidates-batches-table-item/candidates-batches-table-item.component';
import { CandidatesBatchesTableSearchComponent } from './components/candidates-batches-table-search/candidates-batches-table-search.component';
import { EmptySearchResultModule } from '../../../../../../shared/components/empty-search-result';
import { ListHeaderModule } from '../../../../../../shared/components/list-header';
import { ShowMorePanelModule } from '../../../../../../shared/components/show-more-panel';
import { ShowMoreContinuationPanelModule } from '../../../../../../shared/components/show-more-panel-continuation';
import { SharedPipesModule } from '../../../../../../shared/pipes';

@NgModule({
    imports: [
        CommonModule,
        FlexModule,
        MatExpansionModule,
        MatDividerModule,
        SharedPipesModule,
        MatButtonModule,
        ListHeaderModule,
        MatCheckboxModule,
        MatCardModule,
        MatIconModule,
        EmptySearchResultModule,
        MatSnackBarModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        ShowMorePanelModule,
        ShowMoreContinuationPanelModule,
    ],
    declarations: [
        CandidatesBatchesTableComponent,
        CandidatesBatchesTableHeaderComponent,
        CandidatesBatchesTableItemComponent,
        ActionsComponent,
        CandidatesBatchesTableSearchComponent,
    ],
    exports: [CandidatesBatchesTableComponent],
})
export class CandidatesBatchesTableModule {}
