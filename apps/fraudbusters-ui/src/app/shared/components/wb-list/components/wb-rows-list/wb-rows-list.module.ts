import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { ActionsComponent } from './components/actions/actions.component';
import { WbRowsHeaderComponent } from './components/wb-rows-header/wb-rows-header.component';
import { WbRowsItemComponent } from './components/wb-rows-item/wb-rows-item.component';
import { WbRowsListComponent } from './wb-rows-list.component';
import { ListHeaderModule } from '../../../../../shared/components/list-header';
import { SharedPipesModule } from '../../../../../shared/pipes';

@NgModule({
    imports: [
        CommonModule,
        FlexModule,
        MatExpansionModule,
        MatDividerModule,
        SharedPipesModule,
        MatButtonModule,
        ListHeaderModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        ListHeaderModule,
        MatCardModule,
        MatDividerModule,
    ],
    declarations: [
        WbRowsListComponent,
        WbRowsHeaderComponent,
        WbRowsItemComponent,
        ActionsComponent,
        WbRowsItemComponent,
        WbRowsHeaderComponent,
    ],
    exports: [WbRowsListComponent],
})
export class WbRowsListModule {}
