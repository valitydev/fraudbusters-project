import { NgModule } from '@angular/core';

import { WhiteListComponent } from './white-list.component';
import { WbListModule } from '../../../../shared/components/wb-list/wb-list.module';

@NgModule({
    declarations: [WhiteListComponent],
    imports: [WbListModule],
})
export class WhiteListModule {}
