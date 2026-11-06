import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { ListType } from '../../../../api/fb-management/swagger-codegen/model/listType';
import { PayoutListRow } from '../../../../api/fb-management/swagger-codegen/model/payoutListRow';
import { PayoutsService } from '../../../../api/payouts';
import { describePayoutScope, toPayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

interface ListFilter {
    listType: ListType;
    listNames: string[];
    searchValue: string;
}

@Component({
    templateUrl: 'payout-lists.component.html',
    styleUrls: ['../../payouts-section.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutListsComponent {
    /** The tab fixes the list type, as each payment list has its own tab. */
    filter$ = new BehaviorSubject<ListFilter>({
        listType: this.route.snapshot.data.listType as ListType,
        listNames: [],
        searchValue: '',
    });
    listType$ = this.filter$.pipe(map((filter) => filter.listType));
    rows$ = this.filter$.pipe(
        switchMap((filter) => this.payoutsService.findListRows({ ...filter, size: 100 })),
        map((response) => response.result)
    );
    currentListNames$ = this.listType$.pipe(switchMap((type) => this.payoutsService.getCurrentListNames(type)));
    availableListNames$ = this.payoutsService.getAvailableListNames();
    columns$ = this.listType$.pipe(
        map((type) =>
            type === ListType.Grey
                ? ['listName', 'value', 'scope', 'count', 'period', 'actions']
                : ['listName', 'value', 'scope', 'actions']
        )
    );
    nameOptions$ = combineLatest([this.listType$, this.availableListNames$]).pipe(
        map(([type, names]) => (type === ListType.Naming ? [] : names))
    );

    form = this.fb.group({
        listName: ['', Validators.required],
        value: ['', Validators.required],
        partyId: [''],
        walletId: [''],
        count: [null as number | null],
        startCountTime: [''],
        endCountTime: [''],
    });

    constructor(
        private route: ActivatedRoute,
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private fb: FormBuilder
    ) {}

    describeScope(row: PayoutListRow): string {
        return describePayoutScope(row.listRecord ?? {});
    }

    get isGrey(): boolean {
        return this.filter$.value.listType === ListType.Grey;
    }

    get isNaming(): boolean {
        return this.filter$.value.listType === ListType.Naming;
    }

    filterNames(listNames: string[]) {
        this.filter$.next({ ...this.filter$.value, listNames });
    }

    search(searchValue: string) {
        this.filter$.next({ ...this.filter$.value, searchValue: searchValue.trim() });
    }

    save() {
        const { listName, value, partyId, walletId, count, startCountTime, endCountTime } = this.form.getRawValue();
        let scope: { partyId?: string; walletId?: string };
        try {
            scope = toPayoutScope(partyId, walletId);
        } catch (e) {
            this.feedback.failed((e as Error).message);
            return;
        }
        if (this.isGrey && (!count || !endCountTime)) {
            this.feedback.failed('A grey list record needs a limit and an end of the period');
            return;
        }
        const listType = this.filter$.value.listType;
        this.payoutsService
            .saveListRows({
                listType,
                records: [
                    {
                        listRecord: { listName: listName.trim(), value: value.trim(), ...scope },
                        countInfo: this.isGrey
                            ? {
                                  count,
                                  startCountTime: startCountTime ? new Date(startCountTime) : undefined,
                                  endCountTime: new Date(endCountTime),
                              }
                            : undefined,
                    },
                ],
            })
            .subscribe({
                next: () => {
                    this.feedback.done('Record sent to the list');
                    this.form.reset({ listName: '', value: '', partyId: '', walletId: '', count: null });
                    this.filter$.next(this.filter$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }

    remove(row: PayoutListRow) {
        this.feedback
            .confirm(`Remove ${row.listRecord.value} from ${row.listRecord.listName}?`)
            .pipe(switchMap(() => this.payoutsService.removeListRow(row.listType, row.listRecord)))
            .subscribe({
                next: () => {
                    this.feedback.done('Removal sent to the list');
                    this.filter$.next(this.filter$.value);
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
