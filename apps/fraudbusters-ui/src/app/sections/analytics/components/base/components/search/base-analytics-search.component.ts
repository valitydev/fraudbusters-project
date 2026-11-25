import { DatePipe } from '@angular/common';
import { Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, debounceTime, map, switchMap, take } from 'rxjs/operators';

import { DateTimeRange } from '../../../../../../shared/components/datetime-range/datetime-range.component';
import { DateFormat } from '../../../../../../shared/constants/date-format';
import {
    ANALYTICS_PERIODS,
    BaseAnalyticsUtilService,
    DEFAULT_ANALYTICS_PERIOD,
} from '../../services/base-analytics-util.service';
import { BaseAnalyticsService } from '../../services/base-analytics.service';

@Component({
    selector: 'fb-base-analytics-search',
    templateUrl: 'base-analytics-search.component.html',
    styleUrls: ['./base-analytics-search.component.scss'],
})
export class BaseAnalyticsSearchComponent implements OnInit {
    @Input() breakdownFiltered = false;
    @Output() readonly filtersReset = new EventEmitter<void>();

    readonly periods = ANALYTICS_PERIODS;
    readonly currencies$ = this.analytics.currencies$.pipe(catchError(() => of([])));
    readonly form = this.fb.nonNullable.group({
        partyId: '',
        shopId: '',
        type: '',
        time: DEFAULT_ANALYTICS_PERIOD as string,
        from: { value: '', disabled: true },
        to: { value: '', disabled: true },
    });
    readonly now = new Date();
    /** "Reset to 7 days" in the picker: today and the six days before it. */
    readonly sixDays = 6 * 24 * 60 * 60 * 1000;
    private readonly destroyRef = inject(DestroyRef);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private analytics: BaseAnalyticsService,
        private dates: BaseAnalyticsUtilService,
        private datePipe: DatePipe
    ) {}

    get invalidRange(): boolean {
        const { time, from, to } = this.form.getRawValue();
        return time === 'custom' && !this.dates.resolveRange(time, from, to);
    }

    get granularity(): string {
        const { time, from, to } = this.form.getRawValue();
        const unit = this.dates.resolveRange(time, from, to)?.splitUnit;
        return unit ? { hour: 'Hourly (UTC)', day: 'Daily (UTC)', month: 'Monthly (UTC)' }[unit] : '—';
    }

    ngOnInit(): void {
        this.currencies$
            .pipe(
                take(1),
                switchMap((currencies) => this.route.queryParamMap.pipe(map((query) => ({ currencies, query })))),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(({ currencies, query }) => {
                if (currencies?.length) {
                    this.loadFilters(query, currencies);
                }
            });
        this.form.valueChanges.pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            const { partyId, shopId, type, time, from, to } = this.form.getRawValue();
            if (!type || this.invalidRange) {
                return;
            }
            void this.router.navigate([], {
                relativeTo: this.route,
                queryParams: {
                    partyId: partyId.trim() || null,
                    shopId: shopId.trim() || null,
                    type,
                    time,
                    from: time === 'custom' ? from : null,
                    to: time === 'custom' ? to : null,
                },
            });
        });
    }

    setPeriod(time: string): void {
        if (time !== this.form.controls.time.value) {
            const range = time === 'custom' ? null : this.dates.resolveRange(time);
            this.form.patchValue(
                range
                    ? {
                          time,
                          from: this.dates.dateInputValue(range.from),
                          to: this.dates.dateInputValue(range.to),
                      }
                    : { time }
            );
            this.updateDateControls();
        }
    }

    utcDay(value: string): Date | null {
        const date = /^\d{4}-\d{2}-\d{2}$/.test(value ?? '') ? new Date(value + 'T00:00:00Z') : null;
        return date && !Number.isNaN(date.getTime()) ? date : null;
    }

    setCustomRange({ from, to }: DateTimeRange): void {
        this.form.patchValue({
            time: 'custom',
            from: this.dates.dateInputValue(from),
            to: this.dates.dateInputValue(to),
        });
        this.updateDateControls();
    }

    hasActiveFilters(defaultCurrency: string): boolean {
        const { partyId, shopId, type, time } = this.form.getRawValue();
        return !!(partyId || shopId || type !== defaultCurrency || time !== DEFAULT_ANALYTICS_PERIOD);
    }

    resetFilters(defaultCurrency: string): void {
        const range = this.dates.resolveRange(DEFAULT_ANALYTICS_PERIOD);
        this.form.patchValue({
            partyId: '',
            shopId: '',
            type: defaultCurrency,
            time: DEFAULT_ANALYTICS_PERIOD,
            from: this.dates.dateInputValue(range.from),
            to: this.dates.dateInputValue(range.to),
        });
        this.updateDateControls();
        this.filtersReset.emit();
    }

    private updateDateControls(): void {
        const { time, from, to } = this.form.controls;
        if (time.value === 'custom' && from.disabled) {
            from.enable({ emitEvent: false });
            to.enable({ emitEvent: false });
        } else if (time.value !== 'custom' && from.enabled) {
            from.disable({ emitEvent: false });
            to.disable({ emitEvent: false });
        }
    }

    private loadFilters(query: ParamMap, currencies: string[]): void {
        const type = currencies.includes(query.get('type')) ? query.get('type') : currencies[0];
        const time = query.get('time') || DEFAULT_ANALYTICS_PERIOD;
        const partyId = query.get('partyId') || '';
        const shopId = query.get('shopId') || '';
        const fromDate = query.get('from') || '';
        const toDate = query.get('to') || '';
        const range = this.dates.resolveRange(time, fromDate, toDate);
        const from = time === 'custom' ? fromDate : this.dates.dateInputValue(range.from);
        const to = time === 'custom' ? toDate : this.dates.dateInputValue(range.to);
        this.form.setValue({ partyId, shopId, type, time, from, to }, { emitEvent: false });
        this.updateDateControls();
        if (range) {
            this.analytics.search({
                fromTime: this.datePipe.transform(range.from, DateFormat._yyyyMMDdHHMmSs, 'UTC'),
                toTime: this.datePipe.transform(range.to, DateFormat._yyyyMMDdHHMmSs, 'UTC'),
                currency: type,
                merchantId: partyId || undefined,
                shopId: shopId || undefined,
                splitUnit: range.splitUnit,
            });
        }
    }
}
