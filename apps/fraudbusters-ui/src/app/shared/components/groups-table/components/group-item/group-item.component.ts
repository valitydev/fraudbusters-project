import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { GroupListRow } from '../../group-list-row';

@Component({
    // Attribute selector keeps the native table row/head element for table semantics.
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'tr[fb-group-item]',
    templateUrl: 'group-item.component.html',
    styles: [
        'th, td { overflow-wrap: anywhere; }',
        '.group-preview { display: flex; align-items: center; gap: 8px; }',
        '.group-preview code { min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }',
        '.more-templates { flex-shrink: 0; font-size: var(--fb-table-value-font-size); }',
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupItemComponent {
    @Input()
    group: GroupListRow;

    @Output() readonly duplicateItem = new EventEmitter<string>();

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();
}
