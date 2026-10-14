import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { combineLatest, merge, NEVER, Observable, of, Subject } from 'rxjs';
import { catchError, filter, shareReplay, switchMap } from 'rxjs/operators';

import { PaymentGroupsService } from '../../../api/payments/groups';
import { ConfirmActionDialogComponent } from '../../../shared/components/confirm-action-dialog';
import { progress } from '../../../shared/operators';

export interface RemoveGroupParams {
    id: string;
}

@Injectable()
export class RemoveGroupService {
    removed$: Observable<string>;
    inProgress$: Observable<boolean>;

    private removeGroup$ = new Subject<RemoveGroupParams>();
    private hasError$ = new Subject();

    constructor(
        private dialog: MatDialog,
        private paymentGroupsService: PaymentGroupsService,
        private snackBar: MatSnackBar
    ) {
        this.removed$ = this.removeGroup$.pipe(
            switchMap((params) =>
                combineLatest([
                    of(params),
                    this.dialog
                        .open(ConfirmActionDialogComponent, { data: { title: `Remove group ${params.id}?` } })
                        .afterClosed()
                        .pipe(filter((r) => r === 'confirm')),
                ])
            ),
            switchMap(([params]) =>
                this.paymentGroupsService.delete(params.id).pipe(
                    catchError((error: HttpErrorResponse) => {
                        this.snackBar.open(`${error.status}: ${error.message}`, 'OK', {
                            duration: 1500,
                        });
                        this.hasError$.next(undefined);
                        return NEVER;
                    })
                )
            ),
            shareReplay(1)
        );

        this.inProgress$ = progress(this.removeGroup$, merge(this.hasError$, this.removed$));
    }

    remove(params: RemoveGroupParams) {
        this.removeGroup$.next(params);
    }
}
