import { NgModule } from '@angular/core';

import { AddRowGreyListComponent } from './add-row-grey-list/add-row-grey-list.component';
import { GreyListRoutingModule } from './grey-list-routing.module';
import { GreyListComponent } from './grey-list.component';
import { AddRowListModule } from '../../../../shared/components/wb-list/components/add-row-list';
import { WbListModule } from '../../../../shared/components/wb-list/wb-list.module';

@NgModule({
    declarations: [GreyListComponent, AddRowGreyListComponent],
    imports: [GreyListRoutingModule, WbListModule, AddRowListModule],
})
export class GreyListModule {}
