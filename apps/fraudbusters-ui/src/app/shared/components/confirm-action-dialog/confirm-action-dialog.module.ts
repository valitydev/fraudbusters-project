import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogConfig, MatDialogModule, MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material/dialog';

import { ConfirmActionDialogComponent } from './confirm-action-dialog.component';

@NgModule({
    imports: [CommonModule, MatDialogModule, MatButtonModule],
    declarations: [ConfirmActionDialogComponent],
    exports: [ConfirmActionDialogComponent],
    providers: [
        {
            provide: MAT_DIALOG_DEFAULT_OPTIONS,
            useValue: {
                width: '440px',
                maxWidth: 'calc(100vw - 32px)',
                hasBackdrop: true,
                disableClose: true,
                // These options replace Material's defaults, so focus handling is set explicitly:
                // Cancel (first button) gets focus, the opener gets it back on close.
                autoFocus: 'first-tabbable',
                restoreFocus: true,
            } as MatDialogConfig,
        },
    ],
})
export class ConfirmActionDialogModule {}
