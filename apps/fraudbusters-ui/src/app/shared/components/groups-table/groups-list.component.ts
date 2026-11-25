import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { GroupListRow } from './group-list-row';

@Component({
    selector: 'fb-groups-list',
    templateUrl: 'groups-list.component.html',
    styleUrls: ['groups-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupsListComponent {
    @Input() label = 'Payment groups';

    @Input()
    groups: GroupListRow[];

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();

    @Output() readonly duplicateItem = new EventEmitter<string>();

    byId(_index: number, group: GroupListRow): string {
        return group.groupId;
    }
}
