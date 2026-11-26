import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';

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

    readonly view$ = combineLatest({
        logs: this.auditService.logs$.pipe(map((logs) => logs ?? [])),
        busy: this.auditService.inProgress$,
        hasMore: this.auditService.isLoadMore$,
        sort: this.auditService.sort$,
    });

    commandsTypes$ = this.auditService.commandsTypes$;
    objectsTypes$ = this.auditService.objectsTypes$;

    readonly sortOrder = SortOrder;

    constructor(
        private auditService: AuditService,
        private searchFieldService: SearchFieldService,
        private route: ActivatedRoute
    ) {
        combineLatest([this.commandsTypes$, this.objectsTypes$, this.route.queryParams]).subscribe((params) => {
            // Query params come from shared links: fall back to defaults instead of failing on bad values.
            this.filter = {
                user: !params[2].userId ? '' : params[2].userId,
                commandTypes: parseList(params[2].commandTypes, params[0]),
                objectTypes: parseList(params[2].objectTypes, params[1]),
                from: parseDate(params[2].dateFrom) ?? this.searchFieldService.todayFromTime(),
                to: parseDate(params[2].dateTo) ?? new Date(),
            };
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
