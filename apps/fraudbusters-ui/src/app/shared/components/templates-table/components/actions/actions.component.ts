import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { CopyToClipboardService } from '../../../../services/copy-to-clipboard.service';

@Component({
    selector: 'fb-actions',
    templateUrl: 'actions.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class ActionsComponent {
    @Input()
    id: string;

    @Output()
    editItem = new EventEmitter<string>();

    @Output()
    deleteItem = new EventEmitter<string>();

    @Output()
    copyItem = new EventEmitter<string>();

    constructor(private copier: CopyToClipboardService) {}

    copyId(): void {
        this.copier.copy(this.id, 'Template ID');
    }
}
