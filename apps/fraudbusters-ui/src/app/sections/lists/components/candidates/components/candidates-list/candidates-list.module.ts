import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { CandidatesListComponent } from './candidates-list.component';
import { ActionsComponent } from './components/actions/actions.component';
import { CandidateHeaderComponent } from './components/candidate-header/candidate-header.component';
import { CandidateItemComponent } from './components/candidate-item/candidate-item.component';
import { ListHeaderModule } from '../../../../../../shared/components/list-header';
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
    ],
    declarations: [CandidatesListComponent, CandidateHeaderComponent, CandidateItemComponent, ActionsComponent],
    exports: [CandidatesListComponent],
})
export class CandidatesListModule {}
