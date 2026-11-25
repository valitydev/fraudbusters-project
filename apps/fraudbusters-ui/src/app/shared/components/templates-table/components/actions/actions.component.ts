import { Clipboard } from '@angular/cdk/clipboard';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

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

    @Output()
    deleteItem = new EventEmitter<string>();

    @Output()
    copyItem = new EventEmitter<string>();

    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    copyId(): void {
        const copied = this.clipboard.copy(this.id);
        this.snackBar.open(copied ? 'Template ID copied' : 'Could not copy template ID', 'OK', { duration: 2500 });
    }
}
