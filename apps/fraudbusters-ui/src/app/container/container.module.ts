import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterModule } from '@angular/router';

import { FilterRolesPipe } from './components/user-info/filter-roles.pipe';
import { FormatRolePipe } from './components/user-info/format-role.pipe';
import { UserInfoComponent } from './components/user-info/user-info.component';
import { ContainerComponent } from './container.component';
import { NavigateMenuModule } from '../shared/components/navigate-menu/navigate-menu.module';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        MatIconModule,
        MatSidenavModule,
        NavigateMenuModule,
        MatButtonModule,
        MatMenuModule,
        MatDividerModule,
    ],
    declarations: [ContainerComponent, UserInfoComponent, FilterRolesPipe, FormatRolePipe],
    exports: [ContainerComponent],
})
export class ContainerModule {}
