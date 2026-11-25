import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { ActionsComponent } from './components/actions/actions.component';
import { GroupHeaderComponent } from './components/group-header/group-header.component';
import { GroupItemComponent } from './components/group-item/group-item.component';
import { GroupsListComponent } from './groups-list.component';

@NgModule({
    imports: [CommonModule, MatIconModule, MatMenuModule, MatButtonModule],
    declarations: [GroupsListComponent, GroupHeaderComponent, GroupItemComponent, ActionsComponent],
    exports: [GroupsListComponent],
})
export class GroupsListModule {}
