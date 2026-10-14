import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { WbListRecord } from '../../../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { LAYOUT_GAP_M } from '../../../../../tokens';

@Component({
    selector: 'fb-wb-rows-list',
    templateUrl: 'wb-rows-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WbRowsListComponent {
    @Input()
    listRecords: WbListRecord[];

    @Output()
    deleteItem = new EventEmitter<string>();

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
