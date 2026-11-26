import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

export interface ConfirmActionDialogData {
    title?: string;
    /** Optional explanation under the title. */
    message?: string;
    /** Label of the action button, e.g. "Remove"; "Confirm" by default. */
    confirmLabel?: string;
    /** Marks an action that deletes something: the action button turns red. */
    destructive?: boolean;
}

@Component({
    selector: 'fb-confirm-action-dialog',
    templateUrl: 'confirm-action-dialog.component.html',
    styleUrls: ['confirm-action-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class ConfirmActionDialogComponent {
    constructor(
        public dialogRef: MatDialogRef<ConfirmActionDialogComponent, 'cancel' | 'confirm'>,
        @Inject(MAT_DIALOG_DATA)
        public data: ConfirmActionDialogData
    ) {}

    cancel() {
        this.dialogRef.close('cancel');
    }

    confirm() {
        this.dialogRef.close('confirm');
    }
}
