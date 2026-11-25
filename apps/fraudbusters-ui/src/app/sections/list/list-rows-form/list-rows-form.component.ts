import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, DestroyRef, ElementRef, inject, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { Papa } from 'ngx-papaparse';
import { concat, Observable } from 'rxjs';
import { finalize, tap, toArray } from 'rxjs/operators';

import {
    LIST_TYPE_LABELS,
    listRowKey,
    ListRowDraft,
    MANUAL_LIST_TYPES,
    toListRow,
    toPaymentCountInfo,
    toPayoutCountInfo,
} from './list-row-draft';
import { ListType } from '../../../api/fb-management/swagger-codegen/model/listType';
import { PaymentListsService } from '../../../api/payments/lists';
import { PayoutsService } from '../../../api/payouts';
import { CsvPanelComponent, CsvParser } from '../../../shared/components/csv-panel/csv-panel.component';
import {
    DateTimeRange,
    DateTimeRangeComponent,
} from '../../../shared/components/datetime-range/datetime-range.component';
import { ListType as PaymentListType } from '../../../shared/constants/list-type';

/** `datetime-local` value for a moment, in the browser's time zone. */
function localInput(date: Date): string {
    const shifted = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
    return shifted.toISOString().slice(0, 16);
}

/**
 * New rows for payment and payout lists: rows are queued one by one or from CSV,
 * each with its own target list, and created together.
 */
@Component({
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        MatSelectModule,
        MatSnackBarModule,
        CsvPanelComponent,
        DateTimeRangeComponent,
    ],
    templateUrl: 'list-rows-form.component.html',
    styleUrls: [
        '../../../shared/components/reference-form/reference-form.component.scss',
        'list-rows-form.component.scss',
    ],
})
export class ListRowsFormComponent {
    readonly payout = this.route.snapshot.data.payout === true;
    readonly listTypes = this.payout ? [...MANUAL_LIST_TYPES, ListType.Naming] : MANUAL_LIST_TYPES;
    readonly scopeLabel = this.payout ? 'Wallet ID' : 'Shop ID';
    readonly scopeName = this.payout ? 'wallet' : 'shop';
    readonly labels = LIST_TYPE_LABELS;
    readonly listNames$: Observable<string[]>;

    readonly form = this.fb.nonNullable.group({
        listName: [''],
        partyId: [''],
        scopeId: [''],
        value: [''],
        count: [this.payout ? '1' : ''],
        start: [this.payout ? '' : localInput(new Date())],
        end: [this.payout ? '' : localInput(new Date(Date.now() + 24 * 3600_000))],
    });

    listType: ListType;
    staged: ListRowDraft[] = [];
    csvOpen = false;
    saving = false;
    notice = '';
    error = '';

    @ViewChild('listNameInput') private listNameInput: ElementRef<HTMLInputElement>;
    @ViewChild('listNameSelect') private listNameSelect: MatSelect;
    private readonly payoutLists = this.payout ? inject(PayoutsService) : undefined;
    private readonly paymentLists = this.payout ? undefined : inject(PaymentListsService);
    private readonly stagedKeys = new Set<string>();

    constructor(
        private readonly fb: FormBuilder,
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        private readonly snackBar: MatSnackBar,
        private readonly papa: Papa,
        private readonly destroyRef: DestroyRef,
        private readonly cdr: ChangeDetectorRef
    ) {
        this.listNames$ = (this.payoutLists ?? this.paymentLists).getAvailableListNames();
        const requested = route.snapshot.paramMap.get('listType') as ListType;
        this.listType = this.listTypes.includes(requested) ? requested : ListType.Black;
    }

    /** CSV rows go to the selected target list; grey rows also need count and window columns. */
    readonly parseCsv: CsvParser<ListRowDraft> = (text, label) => {
        const parsed = this.papa.parse(text.trim(), {
            header: true,
            skipEmptyLines: 'greedy',
            delimitersToGuess: [',', ';', '\t'],
            transformHeader: (name: string) =>
                name
                    .trim()
                    .toLowerCase()
                    .replace(/[_\s-]/g, ''),
        });
        const otherScope = this.payout ? 'shopid' : 'walletid';
        if (parsed.meta.fields?.includes(otherScope)) {
            return { rows: [], issue: `${label}: use ${this.scopeName}_id for this list.` };
        }
        const rows: ListRowDraft[] = [];
        let skipped = 0;
        for (const row of parsed.data as Record<string, string>[]) {
            const result = toListRow(
                this.listType,
                {
                    listName: row.listname,
                    partyId: row.partyid,
                    scopeId: this.payout ? row.walletid : row.shopid,
                    value: row.value,
                    count: row.count,
                    start: row.startcounttime ?? row.starttime,
                    end: row.endcounttime ?? row.endtime,
                },
                this.payout
            );
            if (typeof result === 'string') {
                skipped++;
            } else {
                rows.push(result);
            }
        }
        const issue = skipped ? `${label}: ${skipped} invalid row${skipped === 1 ? '' : 's'} skipped.` : '';
        return {
            rows,
            issue: rows.length || skipped ? issue : `${label}: no rows found. Use ${this.csvColumns} as the header.`,
        };
    };

    get isNamed(): boolean {
        return this.listType === ListType.Naming;
    }

    get isGrey(): boolean {
        return this.listType === ListType.Grey;
    }

    /** The current input as a row, or the reason it is not one yet. */
    get draft(): ListRowDraft | string {
        return toListRow(this.listType, this.form.getRawValue(), this.payout);
    }

    get draftRow(): ListRowDraft | null {
        const draft = this.draft;
        return typeof draft === 'string' ? null : draft;
    }

    /**
     * A row counts as started once it has a value: list name and party stay filled after Add row
     * so the next value can be typed straight away, and must not block Create on their own.
     */
    get draftStarted(): boolean {
        return Boolean(this.form.getRawValue().value.trim());
    }

    get createCount(): number {
        const draft = this.draftRow;
        return this.staged.length + Number(Boolean(draft && !this.stagedKeys.has(listRowKey(draft))));
    }

    get csvSample(): string {
        const name = this.listType === ListType.Naming ? 'example-list' : 'EMAIL';
        const sample = `${name},example-party,example-${this.scopeName},user@example.test`;
        return this.isGrey ? `${sample},5,2026-01-01T00:00:00Z,2026-01-02T00:00:00Z` : sample;
    }

    get csvColumns(): string {
        const columns = `list_name,party_id,${this.scopeName}_id,value`;
        return this.isGrey ? `${columns},count,start_count_time,end_count_time` : columns;
    }

    inputDate(value: string): Date | null {
        const date = value ? new Date(value) : null;
        return date && !Number.isNaN(date.getTime()) ? date : null;
    }

    setWindow({ from, to }: DateTimeRange): void {
        this.form.patchValue({ start: localInput(from), end: localInput(to) });
    }

    setListType(listType: ListType): void {
        if (!this.saving) {
            this.listType = listType;
            // Parsed CSV rows belong to the previous target: start the import again.
            this.csvOpen = false;
        }
    }

    back(): void {
        if (!this.saving) {
            void this.router.navigateByUrl(
                this.route.snapshot.queryParamMap.get('returnUrl') || this.listsUrl(this.listType)
            );
        }
    }

    addRow(): void {
        const draft = this.draft;
        if (typeof draft === 'string' || this.saving) {
            this.notice = typeof draft === 'string' && this.draftStarted ? draft : '';
            return;
        }
        if (!this.stage(draft)) {
            this.notice = 'This row is already queued.';
            return;
        }
        this.form.patchValue({ value: '' });
        this.notice = '';
        (this.listNameSelect ?? this.listNameInput?.nativeElement)?.focus();
    }

    removeRow(index: number): void {
        const [removed] = this.staged.splice(index, 1);
        this.stagedKeys.delete(listRowKey(removed));
    }

    clearQueue(): void {
        this.staged = [];
        this.stagedKeys.clear();
        this.notice = '';
    }

    addCsvRows(rows: ListRowDraft[]): void {
        const added = rows.filter((row) => this.stage(row)).length;
        const skipped = rows.length - added;
        this.notice = `Added ${added} from CSV.${
            skipped ? ` Skipped ${skipped} duplicate${skipped === 1 ? '' : 's'}.` : ''
        }`;
        this.csvOpen = false;
    }

    create(): void {
        const draft = this.draftRow;
        if (!this.createCount || this.saving || (this.draftStarted && !draft)) {
            return;
        }
        const rows = draft && !this.stagedKeys.has(listRowKey(draft)) ? [...this.staged, draft] : [...this.staged];
        const byType = this.listTypes
            .map((type) => rows.filter((row) => row.listType === type))
            .filter((group) => group.length);
        let saved = 0;
        this.error = '';
        this.saving = true;
        this.form.disable({ emitEvent: false });
        concat(
            ...byType.map((group) =>
                this.saveRows(group).pipe(
                    tap(() => {
                        saved += group.length;
                        const savedKeys = new Set(group.map(listRowKey));
                        this.staged = this.staged.filter((row) => !savedKeys.has(listRowKey(row)));
                        savedKeys.forEach((key) => this.stagedKeys.delete(key));
                        if (draft && savedKeys.has(listRowKey(draft))) {
                            this.form.patchValue({ value: '' });
                        }
                    })
                )
            )
        )
            .pipe(
                toArray(),
                takeUntilDestroyed(this.destroyRef),
                finalize(() => {
                    this.saving = false;
                    this.form.enable({ emitEvent: false });
                    // The app shell is OnPush: async results do not refresh this view by themselves.
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: () => {
                    this.snackBar.open(`${rows.length} ${rows.length === 1 ? 'row' : 'rows'} created`, 'OK', {
                        duration: 3000,
                    });
                    void this.router.navigateByUrl(
                        this.route.snapshot.queryParamMap.get('returnUrl') || this.listsUrl(rows[0].listType)
                    );
                },
                error: ({ status }: HttpErrorResponse) => {
                    const reason =
                        status === 422
                            ? 'The server rejected these rows. Check list names and values.'
                            : 'Could not create the rows. Review the lists before retrying.';
                    this.error = saved ? `${saved} saved. ${reason}` : reason;
                },
            });
    }

    private listsUrl(type: ListType): string {
        const tab = type === ListType.Naming ? 'named' : type;
        return this.payout ? `/payouts/lists/${tab}` : `/lists/${tab}`;
    }

    private saveRows(rows: ListRowDraft[]): Observable<unknown> {
        const listType = rows[0].listType;
        return this.payoutLists
            ? this.payoutLists.saveListRows({ listType, records: rows.map(toPayoutCountInfo) })
            : this.paymentLists.saveListsRows(listType as PaymentListType, rows.map(toPaymentCountInfo));
    }

    private stage(row: ListRowDraft): boolean {
        const key = listRowKey(row);
        if (this.stagedKeys.has(key)) {
            return false;
        }
        this.staged.push(row);
        this.stagedKeys.add(key);
        return true;
    }
}
