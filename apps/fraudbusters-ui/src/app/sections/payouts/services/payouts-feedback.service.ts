import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { ConfirmActionDialogComponent } from '../../../shared/components/confirm-action-dialog';

/** Confirmations and messages shared by the payout configuration tabs. */
@Injectable()
export class PayoutsFeedbackService {
    constructor(private dialog: MatDialog, private snackBar: MatSnackBar) {}

    confirm(title: string): Observable<void> {
        return this.dialog
            .open(ConfirmActionDialogComponent, { data: { title } })
            .afterClosed()
            .pipe(
                filter((result) => result === 'confirm'),
                map(() => undefined)
            );
    }

    done(message: string): void {
        this.snackBar.open(message, 'OK', { duration: 1500 });
    }

    failed(error: HttpErrorResponse | string): void {
        const message = typeof error === 'string' ? error : `${error.status}: ${error.error?.message ?? error.message}`;
        this.snackBar.open(message, 'ERROR', { duration: 3000 });
    }
}
