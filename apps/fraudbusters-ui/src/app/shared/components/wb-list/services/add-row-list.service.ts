import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, merge, Observable, Subject } from 'rxjs';
import { catchError, filter, shareReplay, switchMap } from 'rxjs/operators';

import { progress } from '../../../../shared/operators';
import { CsvUtilsService } from '../../../../shared/services/utils/csv-utils.service';
import { ListType } from '../../../constants/list-type';
import { WbListService } from '../wb-list.service';

@Injectable()
export class AddRowListService {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    created$: Observable<any>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    loadedFile$: Observable<any>;
    inProgress$: Observable<boolean>;
    forms = this.fb.array<FormGroup>([]);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private loadFile$ = new Subject<any>();
    private create$ = new Subject<ListType>();
    private errors$ = new Subject();

    private readonly _sizeByte = 41943040;

    constructor(
        private fb: FormBuilder,
        private wbListService: WbListService,
        private snackBar: MatSnackBar,
        private csvUtilsService: CsvUtilsService
    ) {
        this.addItem();
        this.created$ = this.create$.pipe(
            switchMap((value) => {
                return this.wbListService
                    .saveListRow(
                        value,
                        this.forms.value.map((rec) => ({ listRecord: rec }))
                    )
                    .pipe(
                        catchError((error: HttpErrorResponse) => {
                            this.snackBar.open(`${error.status}: ${error.message}`, 'ERROR');
                            this.errors$.next(undefined);
                            return EMPTY;
                        })
                    );
            }),
            filter((r) => !!r),
            shareReplay(1, 1000)
        );

        this.loadedFile$ = this.loadFile$.pipe(
            switchMap((value) => {
                return this.wbListService.saveListRowsFromFile(value.listType, value.file).pipe(
                    catchError((error: HttpErrorResponse) => {
                        this.snackBar.open(`${error.status}: ${error.message}`, 'ERROR');
                        this.errors$.next(undefined);
                        return EMPTY;
                    })
                );
            }),
            filter((r) => {
                return !!r;
            }),
            shareReplay(1, 1000)
        );

        this.created$.subscribe(() => {
            this.forms = this.fb.array<FormGroup>([]);
            this.addItem();
        });
        this.loadedFile$.subscribe(() => this.snackBar.open('File success send to load', 'OK'));

        this.inProgress$ = progress(
            merge(this.create$, this.loadFile$),
            merge(this.created$, this.errors$, this.loadedFile$)
        );
        this.inProgress$.subscribe((inProgress) => {
            if (inProgress) {
                this.forms.disable();
            } else {
                this.forms.enable();
            }
        });
    }

    create(listType: ListType) {
        this.create$.next(listType);
    }

    addItem() {
        this.forms.push(this.createItem());
    }

    removeItem(i: number) {
        this.forms.removeAt(i);
    }

    saveFileWithListRaws(listType: ListType, file: File): void {
        if (this.csvUtilsService.isValidFile(file, 'text/csv', this._sizeByte)) {
            this.loadFile$.next({ file, listType });
        }
    }

    private createItem(listName = '', partyId = '', shopId = '', value = '') {
        return this.fb.group({
            listName: [listName, Validators.required],
            partyId: [partyId],
            shopId: [shopId],
            value: [value, Validators.required],
        });
    }
}
