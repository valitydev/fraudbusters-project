import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { ListType } from '../../../../api/fb-management/swagger-codegen/model/listType';
import { PayoutCountInfo } from '../../../../api/fb-management/swagger-codegen/model/payoutCountInfo';
import { PayoutsService } from '../../../../api/payouts';
import { LAYOUT_GAP_M } from '../../../../tokens';
import { toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

interface RowValue {
    listName: string;
    partyId: string;
    walletId: string;
    value: string;
    count: number;
    startCountTime: Date | null;
    endCountTime: Date | null;
}

/** Adds rows to one payout list, as the payment new rows page does. */
@Component({
    templateUrl: 'payout-list-rows-form.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutListRowsFormComponent {
    listType = this.route.snapshot.paramMap.get('listType') as ListType;
    isGrey = this.listType === ListType.Grey;
    isNaming = this.listType === ListType.Naming;
    inProgress$ = new BehaviorSubject<boolean>(false);
    listNames$ = this.isNaming ? of([] as string[]) : this.payoutsService.getAvailableListNames();
    forms = new FormArray<FormGroup>([this.createItem()]);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    addItem() {
        this.forms.push(this.createItem());
    }

    removeItem(index: number) {
        this.forms.removeAt(index);
    }

    save() {
        let records: PayoutCountInfo[];
        try {
            records = (this.forms.getRawValue() as RowValue[]).map((row) => this.toRecord(row));
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        this.inProgress$.next(true);
        this.payoutsService
            .saveListRows({ listType: this.listType, records })
            .pipe(finalize(() => this.inProgress$.next(false)))
            .subscribe({
                next: () => {
                    this.feedback.done('Rows sent to the list');
                    void this.router.navigateByUrl(
                        this.route.snapshot.queryParamMap.get('returnUrl') ?? '/payouts/lists'
                    );
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    private toRecord(row: RowValue): PayoutCountInfo {
        const listRecord = {
            listName: row.listName.trim(),
            value: row.value.trim(),
            ...toPayoutScope(row.partyId, row.walletId),
        };
        if (!this.isGrey) {
            return { listRecord };
        }
        return {
            listRecord,
            countInfo: {
                count: Number(row.count),
                startCountTime: row.startCountTime ?? undefined,
                endCountTime: row.endCountTime,
            },
        };
    }

    private createItem(): FormGroup {
        return this.fb.group({
            listName: ['', Validators.required],
            partyId: [''],
            walletId: [''],
            value: ['', Validators.required],
            count: [1, this.isGrey ? [Validators.required, Validators.min(1)] : []],
            startCountTime: [null as Date | null],
            endCountTime: [null as Date | null, this.isGrey ? Validators.required : []],
        });
    }
}
