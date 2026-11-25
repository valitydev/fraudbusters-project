import { NgFor, NgIf } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime } from 'rxjs/operators';

import { AdditionalFilter, HistoricalFilters, HistoricalKind } from './historical-kinds';
import { formatWindow } from '../../../shared/components/datetime-range/datetime-parts';
import {
    DateTimeRange,
    DateTimeRangeComponent,
} from '../../../shared/components/datetime-range/datetime-range.component';

const HOUR = 60 * 60 * 1000;

/** Quick "last N" periods: To becomes now, From goes back by the duration. */
export const HISTORY_PERIODS = [
    { label: '1 hour', duration: HOUR },
    { label: '2 hours', duration: 2 * HOUR },
    { label: '1 day', duration: 24 * HOUR },
    { label: '1 week', duration: 7 * 24 * HOUR },
] as const;

const DEFAULT_PERIOD = 24 * HOUR;

const TEXT_FILTERS = [
    'id',
    'email',
    'partyId',
    'scopeId',
    'cardToken',
    'country',
    'status',
    'fingerprint',
    'customerId',
    'userId',
    'terminal',
    'maskedPan',
    'template',
    'rule',
] as const;

/** Older links name the ID and scope after the operation. */
const LEGACY_PARAMS: Record<string, (typeof TEXT_FILTERS)[number]> = {
    paymentId: 'id',
    withdrawalId: 'id',
    shopId: 'scopeId',
    walletId: 'scopeId',
};

export const ADDITIONAL_LABELS: Record<AdditionalFilter, { label: string; placeholder: string }> = {
    cardToken: { label: 'Card token', placeholder: 'Card token' },
    country: { label: 'Country', placeholder: 'e.g. RU, DE' },
    status: { label: 'Status', placeholder: '' },
    fingerprint: { label: 'Fingerprint', placeholder: 'Device fingerprint' },
    customerId: { label: 'Customer ID', placeholder: 'Customer identifier' },
    userId: { label: 'User ID', placeholder: 'User identifier' },
    terminal: { label: 'Terminal', placeholder: 'Terminal ID' },
    maskedPan: { label: 'Masked PAN', placeholder: 'e.g. 424242******4242' },
    template: { label: 'Template', placeholder: 'Template ID' },
    rule: { label: 'Rule', placeholder: 'e.g. 0' },
};

/** `datetime-local` value (local time, with seconds) for a moment. */
export function toLocalInput(date: Date): string {
    const shifted = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
    return shifted.toISOString().slice(0, 19);
}

function validDate(value: string): Date | null {
    const date = value ? new Date(value) : null;
    return date && !Number.isNaN(date.getTime()) ? date : null;
}

/**
 * Historical data filters: a period with quick presets, the main identifiers and per-tab additional
 * filters. Values live in the query string, so a filtered view can be shared or reloaded.
 */
@Component({
    selector: 'fb-historical-filters',
    standalone: true,
    imports: [NgFor, NgIf, ReactiveFormsModule, MatIconModule, MatSelectModule, DateTimeRangeComponent],
    templateUrl: 'historical-filters.component.html',
    styleUrls: ['historical-filters.component.scss'],
})
export class HistoricalFiltersComponent implements OnInit {
    @Input({ required: true }) kind: HistoricalKind;
    @Output() readonly filtersChange = new EventEmitter<HistoricalFilters>();

    readonly periods = HISTORY_PERIODS;
    readonly labels = ADDITIONAL_LABELS;
    readonly drawerId = 'fb-historical-additional-filters';
    readonly form = this.fb.nonNullable.group({
        from: '',
        to: '',
        ...Object.fromEntries(TEXT_FILTERS.map((name) => [name, ''])),
    } as Record<'from' | 'to' | (typeof TEXT_FILTERS)[number], string>);

    additionalOpen = false;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private destroyRef: DestroyRef
    ) {}

    get isPayout(): boolean {
        return this.kind.operation === 'payout';
    }

    get scopeLabel(): string {
        return this.isPayout ? 'Wallet ID' : 'Shop ID';
    }

    /** Filled additional filters, so a value is never hidden without a trace. */
    get additionalCount(): number {
        const value = this.form.getRawValue();
        return this.kind.additional.filter((name) => Boolean(value[name]?.trim())).length;
    }

    get fromDate(): Date | null {
        return validDate(this.form.getRawValue().from);
    }

    get toDate(): Date | null {
        return validDate(this.form.getRawValue().to);
    }

    get windowText(): string {
        const from = this.fromDate;
        const to = this.toDate;
        return from && to ? formatWindow(from, to) : '';
    }

    get isCustomPeriod(): boolean {
        return !this.periods.some((period) => this.isPeriod(period.duration));
    }

    ngOnInit(): void {
        const params = this.route.snapshot.queryParamMap;
        const values: Partial<Record<string, string>> = {};
        for (const name of TEXT_FILTERS) {
            values[name] = params.get(name) ?? '';
        }
        for (const [legacy, name] of Object.entries(LEGACY_PARAMS)) {
            values[name] ||= params.get(legacy) ?? '';
        }
        const from = validDate(params.get('from') ?? '');
        const to = validDate(params.get('to') ?? '');
        const end = to ?? new Date();
        this.form.setValue(
            {
                ...(values as Record<(typeof TEXT_FILTERS)[number], string>),
                from: toLocalInput(from ?? new Date(end.getTime() - DEFAULT_PERIOD)),
                to: toLocalInput(end),
            },
            { emitEvent: false }
        );
        this.additionalOpen = this.additionalCount > 0;
        this.form.valueChanges
            .pipe(debounceTime(400), takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.emit());
        this.emit();
    }

    setPeriod(duration: number): void {
        const to = new Date();
        this.form.patchValue({
            from: toLocalInput(new Date(to.getTime() - duration)),
            to: toLocalInput(to),
        });
    }

    /** A preset stays selected while From / To are that far apart (to the second). */
    isPeriod(duration: number): boolean {
        const { from, to } = this.form.getRawValue();
        const start = validDate(from);
        const end = validDate(to);
        return Boolean(start && end) && Math.abs(end.getTime() - start.getTime() - duration) < 1000;
    }

    setRange({ from, to }: DateTimeRange): void {
        this.form.patchValue({ from: toLocalInput(from), to: toLocalInput(to) });
    }

    toggleAdditional(): void {
        this.additionalOpen = !this.additionalOpen;
    }

    private emit(): void {
        const value = this.form.getRawValue();
        const from = validDate(value.from);
        const to = validDate(value.to);
        if (!from || !to || from > to) {
            return;
        }
        // Only the filters this tab shows are sent; a value typed on another tab stays in the form.
        const visible = new Set<string>(['id', 'email', 'partyId', 'scopeId', ...this.kind.additional]);
        const filters: HistoricalFilters = { from: from.toISOString(), to: to.toISOString() };
        for (const name of TEXT_FILTERS) {
            const text = value[name]?.trim();
            if (text && visible.has(name)) {
                filters[name] = text;
            }
        }
        // The page has no other query parameters: the filters replace them, dropping old names.
        void this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { ...filters },
            replaceUrl: true,
        });
        this.filtersChange.emit(filters);
    }
}
