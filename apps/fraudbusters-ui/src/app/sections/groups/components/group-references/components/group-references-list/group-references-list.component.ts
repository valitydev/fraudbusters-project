import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { GroupReference } from '../../../../../../api/fb-management/swagger-codegen/model/groupReference';
import { ReferenceRow } from '../../../../../../shared/components/reference-table/reference-table.component';

@Component({
    selector: 'fb-group-references-list',
    templateUrl: 'group-references-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupReferencesListComponent {
    rows: ReferenceRow<GroupReference>[] = [];

    @Input()
    set references(references: GroupReference[]) {
        this.rows = references.map((source) => ({
            ...source,
            targetId: source.groupId,
            scopeId: source.shopId,
            source,
        }));
    }

    @Output()
    deleteItem = new EventEmitter<GroupReference>();
}
