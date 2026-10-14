import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { Group } from '../../../../../../api/fb-management/swagger-codegen/model/group';
import { LAYOUT_GAP_M } from '../../../../../../tokens';

@Component({
    selector: 'fb-groups-list',
    templateUrl: 'groups-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupsListComponent {
    @Input()
    groups: Group[];

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
