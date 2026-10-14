import { ChangeDetectionStrategy, Component, EventEmitter, Inject, Input, Output } from '@angular/core';

import { GroupReference } from '../../../../../../api/fb-management/swagger-codegen/model/groupReference';
import { LAYOUT_GAP_M } from '../../../../../../tokens';

@Component({
    selector: 'fb-group-references-list',
    templateUrl: 'group-references-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupReferencesListComponent {
    @Input()
    references: GroupReference[];

    @Output()
    goToGroup = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<GroupReference>();

    constructor(@Inject(LAYOUT_GAP_M) public layoutGapM: string) {}
}
