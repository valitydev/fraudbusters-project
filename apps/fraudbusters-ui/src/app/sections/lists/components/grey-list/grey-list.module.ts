import { NgModule } from '@angular/core';

import { GreyListComponent } from './grey-list.component';
import { WbListModule } from '../../../../shared/components/wb-list/wb-list.module';

@NgModule({
    declarations: [GreyListComponent],
    imports: [WbListModule],
})
export class GreyListModule {}
