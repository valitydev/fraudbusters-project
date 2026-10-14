import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { combineLatest, EMPTY, merge, NEVER, Observable, of, Subject } from 'rxjs';
import { catchError, filter, shareReplay, switchMap } from 'rxjs/operators';

import { PaymentListsService } from '../../../../api/payments/lists';
import { progress } from '../../../../shared/operators';
import { ListType } from '../../../constants/list-type';
import { CsvUtilsService } from '../../../services/utils/csv-utils.service';
import { ConfirmActionDialogComponent } from '../../confirm-action-dialog';

export interface RemoveListRowParams {
    rowId: string;
}

@Injectable()
export class RemoveWbListComponentService {
    removed$: Observable<string>;
    inProgress$: Observable<boolean>;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    loadedFile$: Observable<any>;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private loadFile$ = new Subject<any>();
    private removeRow$ = new Subject<RemoveListRowParams>();

    // eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
    private hasError$ = new Subject<unknown | undefined>();

    private readonly _sizeByte = 41943040;

    constructor(
        private dialog: MatDialog,
        private paymentListsService: PaymentListsService,
        private snackBar: MatSnackBar,
        private csvUtilsService: CsvUtilsService
    ) {
        this.removed$ = this.removeRow$.pipe(
            switchMap((params) =>
                combineLatest([
                    of(params),
                    this.dialog
                        .open(ConfirmActionDialogComponent, { data: { title: `Remove row ${params.rowId}?` } })
                        .afterClosed()
                        .pipe(filter((r) => r === 'confirm')),
                ])
            ),
            switchMap(([params]) =>
                this.paymentListsService.deleteListRow(params.rowId).pipe(
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

        this.loadedFile$ = this.loadFile$.pipe(
            switchMap((value) => {
                return this.paymentListsService.deleteListsRowsByFile(value.listType, value.file).pipe(
                    catchError((error: HttpErrorResponse) => {
                        this.snackBar.open(`${error.status}: ${error.message}`, 'ERROR');
                        this.hasError$.next(undefined);
                        return EMPTY;
                    })
                );
            }),
            filter((r) => {
                return !!r;
            }),
            shareReplay(1, 1000)
        );

        this.loadedFile$.subscribe(() => this.snackBar.open('File success send to load', 'OK'));

        this.inProgress$ = progress(this.removeRow$, merge(this.hasError$, this.removed$));
    }

    deleteByFileWithListRaws(listType: ListType, file: File): void {
        if (this.csvUtilsService.isValidFile(file, 'text/csv', this._sizeByte)) {
            this.loadFile$.next({ file, listType });
        }
    }

    remove(params: RemoveListRowParams) {
        this.removeRow$.next(params);
    }
}
