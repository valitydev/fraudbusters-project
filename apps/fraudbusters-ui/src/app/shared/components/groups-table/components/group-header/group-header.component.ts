import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    // Attribute selector keeps the native table row/head element for table semantics.
    // eslint-disable-next-line @angular-eslint/component-selector
    selector: 'tr[fb-group-header]',
    templateUrl: 'group-header.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class GroupHeaderComponent {}
