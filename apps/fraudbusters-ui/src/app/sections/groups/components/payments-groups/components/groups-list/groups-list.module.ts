import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';

import { ActionsComponent } from './components/actions/actions.component';
import { GroupHeaderComponent } from './components/group-header/group-header.component';
import { GroupItemComponent } from './components/group-item/group-item.component';
import { GroupsListComponent } from './groups-list.component';
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
    declarations: [GroupsListComponent, GroupHeaderComponent, GroupItemComponent, ActionsComponent],
    exports: [GroupsListComponent],
})
export class GroupsListModule {}
