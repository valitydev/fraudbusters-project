import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { distinctUntilChanged, map } from 'rxjs/operators';

import { DateTimeRange } from '../../../../shared/components/datetime-range/datetime-range.component';
import { DateFormat } from '../../../../shared/constants/date-format';
import { SearchFieldService } from '../../../../shared/services/utils/search-field.service';
import { FetchDataSetsService } from '../../services/payment-data-sets/fetch-data-sets.service';
import { RemoveDataSetsService } from '../../services/payment-data-sets/remove-data-sets.service';

@Component({
    templateUrl: 'payment-data-sets.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentDataSetsComponent {
    readonly dataSets$ = this.fetchDataSetsService.searchResult$;
    readonly inProgress$ = this.fetchDataSetsService.inProgress$;
    readonly hasMore$ = this.fetchDataSetsService.hasMore$;
    readonly failed$ = this.fetchDataSetsService.failed$;
    readonly range: DateTimeRange;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private fetchDataSetsService: FetchDataSetsService,
        private removeDataSetsService: RemoveDataSetsService,
        datePipe: DatePipe,
        searchFields: SearchFieldService,
        destroyRef: DestroyRef,
        cdr: ChangeDetectorRef
    ) {
        const today = searchFields.todayFromTime();
        const now = new Date();
        this.range = { from: today, to: now };
        route.queryParamMap
            .pipe(
                map((params) => ({
                    from: params.get('from'),
                    to: params.get('to'),
                    searchValue: params.get('searchValue')?.trim(),
                })),
                distinctUntilChanged((a, b) => a.from === b.from && a.to === b.to && a.searchValue === b.searchValue),
                takeUntilDestroyed(destroyRef)
            )
            .subscribe(({ from, to, searchValue }) => {
                this.range.from = this.parseDate(from, today);
                this.range.to = this.parseDate(to, now);
                fetchDataSetsService.search({
                    searchValue,
                    from: datePipe.transform(this.range.from, DateFormat._yyyyMMDdHHMmSs),
                    to: datePipe.transform(this.range.to, DateFormat._yyyyMMDdHHMmSs),
                });
                cdr.markForCheck();
            });
        this.removeDataSetsService.removed$.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => this.reload());
    }

    setRange({ from, to }: DateTimeRange): void {
        void this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { from: from.toISOString(), to: to.toISOString() },
            queryParamsHandling: 'merge',
            replaceUrl: true,
        });
    }

    reload(): void {
        this.fetchDataSetsService.refresh();
    }

    fetchMore(): void {
        this.fetchDataSetsService.fetchMore();
    }

    goToDataSet(id: string): void {
        void this.router.navigate(['/data-sets', id, 'testing']);
    }

    removeDataSet(id: string): void {
        this.removeDataSetsService.remove({ dataSet: { id } });
    }

    private parseDate(value: string | null, fallback: Date): Date {
        const timestamp = Date.parse(value ?? '');
        return Number.isFinite(timestamp) ? new Date(timestamp) : fallback;
    }
}
