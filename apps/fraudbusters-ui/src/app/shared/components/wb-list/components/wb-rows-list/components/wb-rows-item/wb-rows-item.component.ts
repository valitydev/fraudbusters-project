import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { WbListRecord } from '../../../../../../../api/fb-management/swagger-codegen/model/wbListRecord';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../tokens';

@Component({
    selector: 'fb-wb-rows-item',
    templateUrl: 'wb-rows-item.component.html',
    styleUrls: ['wb-rows-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WbRowsItemComponent {
    @Input()
    listRecord: WbListRecord;

    @Output()
    deleteItem = new EventEmitter<string>();

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
