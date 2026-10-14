import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { Group } from '../../../../../../../../api/fb-management/swagger-codegen/model/group';
import { LAYOUT_GAP_M, LAYOUT_GAP_S } from '../../../../../../../../tokens';

@Component({
    selector: 'fb-group-item',
    templateUrl: 'group-item.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupItemComponent {
    @Input()
    group: Group;

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();

    constructor(@Inject(LAYOUT_GAP_S) public layoutGapS: string, @Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
