import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, Observable, Subject } from 'rxjs';
import { catchError, filter, shareReplay, switchMap } from 'rxjs/operators';

import { Group } from '../../../../api/fb-management/swagger-codegen/model/group';
import { PriorityId } from '../../../../api/fb-management/swagger-codegen/model/priorityId';
import { PaymentGroupsService } from '../../../../api/payments/groups';
import { progress } from '../../../operators';

@Injectable()
export class GroupService {
    inProgress$: Observable<boolean>;
    saved$: Observable<string>;
    form = this.fb.group({
        id: '',
    });
    forms = this.fb.array<FormGroup>([]);

    private save$ = new Subject<Group>();
    private errors$ = new Subject();

    constructor(
        private fb: FormBuilder,
        private paymentGroupService: PaymentGroupsService,
        private snackBar: MatSnackBar
    ) {
        this.addItem();
        this.save$.subscribe();
        this.saved$ = this.save$.pipe(
            switchMap(() =>
                this.paymentGroupService
                    .save({ groupId: this.form.value.id, priorityTemplates: this.forms.value })
                    .pipe(
                        catchError((error: HttpErrorResponse) => {
                            this.snackBar.open(`${error.status}: ${error.message}`, 'ERROR');
                            this.errors$.next(undefined);
                            return EMPTY;
                        })
                    )
            ),
            filter((r) => !!r),
            shareReplay(1)
        );
        this.inProgress$ = progress(this.save$, this.saved$);
        this.inProgress$.subscribe((inProgress) => {
            if (inProgress) {
                this.forms.disable();
            } else {
                this.forms.enable();
            }
        });
    }

    save() {
        this.save$.next(undefined);
    }

    addItem() {
        this.forms.push(this.createItem());
    }

    clear() {
        this.forms.clear();
    }

    addItems(priorityIds: PriorityId[]) {
        priorityIds.forEach((priorityId) => this.forms.push(this.createItem(priorityId.id, priorityId.priority)));
    }

    removeItem(i: number) {
        this.forms.removeAt(i);
    }

    private createItem(id = '', priority = 0) {
        return this.fb.group({
            id: [id, Validators.required],
            priority: [priority, Validators.required],
        });
    }
}
