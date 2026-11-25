import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, Observable, Subject } from 'rxjs';
import { catchError, filter, map, share, switchMap } from 'rxjs/operators';

import { PaymentListsService } from '../../../../api/payments/lists';
import { ConfirmActionDialogComponent } from '../../confirm-action-dialog';

export interface RemoveListRowParams {
    rowId: string;
    /** Dialog title; defaults to the row ID. */
    title?: string;
}

/** Asks for confirmation and removes one list row; `removed$` emits the removed row ID. */
@Injectable()
export class RemoveWbListComponentService {
    readonly removed$: Observable<string>;

    private removeRow$ = new Subject<RemoveListRowParams>();

    constructor(
        private dialog: MatDialog,
        private paymentListsService: PaymentListsService,
        private snackBar: MatSnackBar
    ) {
        this.removed$ = this.removeRow$.pipe(
            switchMap((params) =>
                this.dialog
                    .open(ConfirmActionDialogComponent, {
                        data: {
                            confirmLabel: 'Remove',
                            destructive: true,
                            title: params.title ?? `Remove row ${params.rowId}?`,
                        },
                    })
                    .afterClosed()
                    .pipe(
                        filter((result) => result === 'confirm'),
                        map(() => params.rowId)
                    )
            ),
            switchMap((rowId) =>
                this.paymentListsService.deleteListRow(rowId).pipe(
                    catchError((error: HttpErrorResponse) => {
                        this.snackBar.open(`${error.status}: ${error.message}`, 'OK', { duration: 3000 });
                        return EMPTY;
                    })
                )
            ),
            share()
        );
    }

    remove(params: RemoveListRowParams): void {
        this.removeRow$.next(params);
    }
}
