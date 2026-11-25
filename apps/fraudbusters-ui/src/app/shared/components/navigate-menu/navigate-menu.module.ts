import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

import { MenuListItemComponent } from './menu-list-item/menu-list-item.component';
import { NavigateMenuComponent } from './navigate-menu.component';

@NgModule({
    declarations: [NavigateMenuComponent, MenuListItemComponent],
    exports: [NavigateMenuComponent],
    imports: [CommonModule, MatIconModule, RouterModule],
    providers: [],
})
export class NavigateMenuModule {}
