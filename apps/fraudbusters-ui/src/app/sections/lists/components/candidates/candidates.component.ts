import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { CandidateBatchRow, candidateBatchRow } from './candidate-batch';
import { PaymentListsService } from '../../../../api/payments/lists';
import { CatalogPanelComponent } from '../../../../shared/components/catalog-panel/catalog-panel.component';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { CopyToClipboardService } from '../../../../shared/services/copy-to-clipboard.service';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';

/** The backend returns batches without a total; at most this many are shown. */
export const CANDIDATE_BATCH_PAGE = 100;

interface BatchesView {
    rows: CandidateBatchRow[];
    busy: boolean;
    failed: boolean;
    truncated: boolean;
}

/** Batches of list candidates found by chargeback or velocity checks, waiting for review. */
@Component({
    imports: [
        AsyncPipe,
        DatePipe,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        CatalogPanelComponent,
    ],
    templateUrl: './candidates.component.html',
    styleUrls: ['./candidates.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CandidatesComponent {
    readonly sortOptions = [
        { value: SortOrder.Desc, label: 'Created at (newest)' },
        { value: SortOrder.Asc, label: 'Created at (oldest)' },
    ];

    readonly sort$ = new BehaviorSubject<SortOrder>(SortOrder.Desc);

    readonly view$: Observable<BatchesView>;

    private readonly reload$ = new BehaviorSubject<void>(undefined);

    constructor(
        private router: Router,
        route: ActivatedRoute,
        private listService: PaymentListsService,
        private copier: CopyToClipboardService
    ) {
        const loaded$ = combineLatest([catalogSearchQuery(route), this.reload$]).pipe(
            switchMap(([searchValue]) =>
                this.listService
                    .findCandidatesBatch({
                        sortOrder: SortOrder.Desc,
                        size: CANDIDATE_BATCH_PAGE,
                        ...(searchValue ? { searchValue } : {}),
                    })
                    .pipe(
                        map(({ result }) => ({ rows: (result ?? []).map(candidateBatchRow), failed: false })),
                        catchError(() => of({ rows: [] as CandidateBatchRow[], failed: true })),
                        map((page) => ({ ...page, busy: false })),
                        startWith({ rows: null as CandidateBatchRow[], failed: false, busy: true })
                    )
            ),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        let lastRows: CandidateBatchRow[] = [];
        this.view$ = combineLatest([loaded$, this.sort$]).pipe(
            map(([page, sort]) => {
                // Keep the previous rows on screen while a new search is loading.
                lastRows = page.rows ?? lastRows;
                const direction = sort === SortOrder.Asc ? 1 : -1;
                const rows = [...lastRows].sort(
                    (a, b) => (this.time(a.createdAt) - this.time(b.createdAt)) * direction
                );
                return {
                    rows,
                    busy: page.busy,
                    failed: page.failed,
                    truncated: rows.length >= CANDIDATE_BATCH_PAGE,
                };
            })
        );
    }

    get returnUrl(): string {
        return this.router.url;
    }

    setSort(sort: SortOrder): void {
        this.sort$.next(sort);
    }

    reload(): void {
        this.reload$.next();
    }

    copyId(id: string): void {
        this.copier.copy(id, 'Batch ID');
    }

    private time(date?: Date | string): number {
        return date ? new Date(date).getTime() : 0;
    }
}
