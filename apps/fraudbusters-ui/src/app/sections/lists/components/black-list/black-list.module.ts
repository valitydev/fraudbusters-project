import { NgModule } from '@angular/core';

import { BlackListComponent } from './black-list.component';
import { WbListModule } from '../../../../shared/components/wb-list/wb-list.module';

@NgModule({
    declarations: [BlackListComponent],
    imports: [WbListModule],
})
export class BlackListModule {}
