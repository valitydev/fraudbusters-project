import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, defer, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { AuditService } from './audit.service';
import { Filter } from './model/filter';
import { DateTimeRange } from '../../shared/components/datetime-range/datetime-range.component';
import { SortOrder } from '../../shared/constants/sort-order';
import { SearchFieldService } from '../../shared/services/utils/search-field.service';
import { statusPillClass } from '../../shared/utils/status-tone';

@Component({
    templateUrl: './audit.component.html',
    styleUrls: ['./audit.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class AuditComponent {
    filter: Filter;

    readonly view$ = defer(() =>
        combineLatest({
            logs: this.auditService.logs$.pipe(
                map((logs) => logs ?? []),
                startWith([])
            ),
            busy: this.auditService.inProgress$,
            hasMore: this.auditService.isLoadMore$,
            sort: this.auditService.sort$,
            options: this.filterOptions$,
        })
    ).pipe(map((view) => ({ ...view, busy: view.busy || view.options === undefined })));
    readonly sortOrder = SortOrder;

    private readonly loadFilterOptions$ = new BehaviorSubject<void>(undefined);
    private readonly filterOptions$ = this.loadFilterOptions$.pipe(
        switchMap(() =>
            combineLatest({
                commands: this.auditService.commandsTypes$,
                objects: this.auditService.objectsTypes$,
            }).pipe(
                startWith(undefined),
                catchError(() => of(null))
            )
        ),
        shareReplay({ bufferSize: 1, refCount: true })
    );

    constructor(
        private auditService: AuditService,
        searchFieldService: SearchFieldService,
        route: ActivatedRoute,
        changeDetector: ChangeDetectorRef
    ) {
        const defaultFrom = searchFieldService.todayFromTime();
        const defaultTo = new Date();
        combineLatest([route.queryParams, this.filterOptions$])
            .pipe(takeUntilDestroyed())
            .subscribe(([params, options]) => {
                // Query params come from shared links: fall back to defaults instead of failing on bad values.
                this.filter = {
                    user: params.userId || '',
                    commandTypes: parseList(params.commandTypes, options?.commands ?? []),
                    objectTypes: parseList(params.objectTypes, options?.objects ?? []),
                    from: parseDate(params.dateFrom) ?? defaultFrom,
                    to: parseDate(params.dateTo) ?? defaultTo,
                };
                changeDetector.markForCheck();
                if (!options) {
                    return;
                }
                this.auditService.mergeQueryParam({
                    dateTo: this.filter.to,
                    dateFrom: this.filter.from,
                    userId: this.filter.user,
                    commandTypes: this.filter.commandTypes ? JSON.stringify(this.filter.commandTypes) : null,
                    objectTypes: this.filter.objectTypes ? JSON.stringify(this.filter.objectTypes) : null,
                });
                this.auditService.searchFilter$.next(this.filter);
            });
    }

    /** Total matching changes reported with the last page. */
    get total(): number {
        return this.auditService.count;
    }

    setSort(order: string): void {
        this.auditService.sort$.next(order as SortOrder);
    }

    loadMore(): void {
        this.auditService.loadMoreAction$.next(true);
    }

    refresh(): void {
        this.auditService.loadMoreAction$.next(false);
    }

    retryFilterOptions(): void {
        this.loadFilterOptions$.next();
    }

    selectionCommand(): void {
        this.auditService.mergeQueryParam({
            commandTypes: JSON.stringify(this.filter.commandTypes),
        });
    }

    selectionObject(): void {
        this.auditService.mergeQueryParam({
            objectTypes: JSON.stringify(this.filter.objectTypes),
        });
    }

    search($event): void {
        this.auditService.searchField$.next($event.target.value);
    }

    setRange({ from, to }: DateTimeRange): void {
        this.auditService.mergeQueryParam({ dateFrom: from.toISOString(), dateTo: to.toISOString() });
    }

    /** "All objects", a single value, or the first one and how many more, so the field never overflows. */
    selectionLabel(selected: string[] | null, all: string[] | null, allLabel: string): string {
        const values = selected ?? [];
        if (!values.length || (all?.length && values.length === all.length)) {
            return allLabel;
        }
        const first = values[0].toLowerCase();
        return values.length === 1 ? first : `${first} +${values.length - 1} more`;
    }

    pill(commandType: string): string {
        return statusPillClass(commandType);
    }
}

function parseList(raw: string | undefined, fallback: string[]): string[] {
    if (!raw) {
        return fallback;
    }
    try {
        const value: unknown = JSON.parse(raw);
        return Array.isArray(value) && value.every((item) => typeof item === 'string') ? value : fallback;
    } catch {
        return fallback;
    }
}

function parseDate(raw: string | undefined): Date | null {
    const date = raw ? new Date(raw) : null;
    return date && !Number.isNaN(date.getTime()) ? date : null;
}
