import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'fb-actions',
    templateUrl: 'actions.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActionsComponent {
    @Input()
    id: string;

    @Output()
    editItem = new EventEmitter<string>();

    @Output() readonly duplicateItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();
}
