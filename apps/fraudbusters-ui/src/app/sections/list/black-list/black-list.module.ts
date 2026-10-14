import { NgModule } from '@angular/core';

import { AddRowBlackListComponent } from './add-row-black-list/add-row-black-list.component';
import { BlackListRoutingModule } from './black-list-routing.module';
import { BlackListComponent } from './black-list.component';
import { AddRowListModule } from '../../../shared/components/wb-list/components/add-row-list/add-row-list.module';
import { WbListModule } from '../../../shared/components/wb-list/wb-list.module';

@NgModule({
    declarations: [BlackListComponent, AddRowBlackListComponent],
    imports: [BlackListRoutingModule, WbListModule, AddRowListModule],
})
export class BlackListModule {}
