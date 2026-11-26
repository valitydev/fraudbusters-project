import { DatePipe, LowerCasePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    OnDestroy,
    TemplateRef,
    ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { of, Subject } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';

import { HistoricalActionsService } from './historical-actions.service';
import { toHistoricalCsv } from './historical-csv';
import { HistoricalFiltersComponent } from './historical-filters.component';
import {
    HISTORICAL_KINDS,
    HistoricalApis,
    HistoricalFilters,
    HistoricalKind,
    HistoricalPage,
} from './historical-kinds';
import { Chargeback } from '../../../api/fb-management/swagger-codegen/model/chargeback';
import { Payment } from '../../../api/fb-management/swagger-codegen/model/payment';
import { DataSetService } from '../../../api/payments/data-set';
import { HistoricalDataService } from '../../../api/payments/historical-data';
import { PaymentListsService } from '../../../api/payments/lists';
import { PayoutsService } from '../../../api/payouts';
import { ConfigService } from '../../../config';
import { ProfileService } from '../../../container/services/profile/profile.service';
import {
    ConfirmActionDialogComponent,
    ConfirmActionDialogModule,
} from '../../../shared/components/confirm-action-dialog';
import { HistoricalRow } from '../../../shared/components/historical-table/historical-row';
import { HistoricalTableComponent } from '../../../shared/components/historical-table/historical-table.component';
import { SortOrder } from '../../../shared/constants/sort-order';

interface LoadRequest {
    filters: HistoricalFilters;
    sortOrder: SortOrder;
    continuationId?: string;
}

/** One historical tab: filters, the records table with paging, selection actions and CSV export. */
@Component({
    imports: [
        LowerCasePipe,
        RouterLink,
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        MatSelectModule,
        MatProgressBarModule,
        MatSnackBarModule,
        ConfirmActionDialogModule,
        HistoricalFiltersComponent,
        HistoricalTableComponent,
    ],
    templateUrl: 'historical-records.component.html',
    styleUrls: ['historical-records.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalRecordsComponent implements AfterViewInit, OnDestroy {
    @ViewChild('headerActions') headerActions: TemplateRef<unknown>;

    readonly kind: HistoricalKind;
    readonly sortOptions = [
        { value: SortOrder.Desc, label: 'Event time (newest)' },
        { value: SortOrder.Asc, label: 'Event time (oldest)' },
    ];

    rows: HistoricalRow[] = [];
    continuationId?: string;
    busy = false;
    failed = false;
    searched = false;
    working = false;
    sortOrder = SortOrder.Desc;
    selected = new Set<string>();

    private filters?: HistoricalFilters;
    private readonly load$ = new Subject<LoadRequest>();
    private readonly apis: HistoricalApis;

    constructor(
        route: ActivatedRoute,
        payments: HistoricalDataService,
        payouts: PayoutsService,
        datePipe: DatePipe,
        private config: ConfigService,
        private router: Router,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private lists: PaymentListsService,
        private dataSets: DataSetService,
        private profile: ProfileService,
        private actions: HistoricalActionsService,
        private cdr: ChangeDetectorRef,
        private destroyRef: DestroyRef
    ) {
        this.kind = HISTORICAL_KINDS[route.snapshot.data.kind as keyof typeof HISTORICAL_KINDS];
        this.apis = { payments, payouts, datePipe };
        this.load$
            .pipe(
                // A new search or sort drops a page that is still loading.
                switchMap((request) =>
                    this.kind.fetch(this.apis, { ...request, size: this.config.pageSize }).pipe(
                        map((page): { request: LoadRequest; page: HistoricalPage | null } => ({ request, page })),
                        catchError(() => of({ request, page: null }))
                    )
                ),
                takeUntilDestroyed(destroyRef)
            )
            .subscribe(({ request, page }) => this.apply(request, page));
    }

    get selectedRows(): HistoricalRow[] {
        return this.rows.filter((row) => this.selected.has(row.key));
    }

    get selectionHint(): string {
        return this.kind.selection === 'candidates'
            ? 'Selected chargebacks go to Lists → Candidates for review before anything is added to a list.'
            : 'Selected payments become a data set you can replay against templates in Testing.';
    }

    ngAfterViewInit(): void {
        this.actions.set(this.headerActions);
    }

    ngOnDestroy(): void {
        this.actions.clear(this.headerActions);
    }

    search(filters: HistoricalFilters): void {
        this.filters = filters;
        this.reload();
    }

    setSort(order: string): void {
        this.sortOrder = order as SortOrder;
        this.reload();
    }

    reload(): void {
        if (!this.filters) {
            return;
        }
        this.busy = true;
        this.failed = false;
        this.selected = new Set();
        this.load$.next({ filters: this.filters, sortOrder: this.sortOrder });
    }

    more(): void {
        if (this.filters && this.continuationId && !this.busy) {
            this.busy = true;
            this.load$.next({ filters: this.filters, sortOrder: this.sortOrder, continuationId: this.continuationId });
        }
    }

    toggle(row: HistoricalRow): void {
        const next = new Set(this.selected);
        if (!next.delete(row.key)) {
            next.add(row.key);
        }
        this.selected = next;
    }

    toggleAll(select: boolean): void {
        this.selected = select ? new Set(this.rows.map((row) => row.key)) : new Set();
    }

    clearSelection(): void {
        this.selected = new Set();
    }

    exportCsv(): void {
        if (!this.rows.length) {
            return;
        }
        const blob = new Blob([toHistoricalCsv(this.kind, this.rows)], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.kind.key}-${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')}.csv`;
        link.click();
        URL.revokeObjectURL(url);
    }

    runSelectionAction(): void {
        if (this.kind.selection === 'candidates') {
            this.sendToCandidates();
        } else if (this.kind.selection === 'data-set') {
            this.createDataSet();
        }
    }

    private sendToCandidates(): void {
        const chargebacks = this.selectedRows.map((row) => row.source as Chargeback);
        const noun = chargebacks.length === 1 ? 'chargeback' : 'chargebacks';
        this.dialog
            .open(ConfirmActionDialogComponent, {
                data: {
                    title: `Send ${chargebacks.length} ${noun} to list candidates?`,
                    message:
                        'Their entities appear in Lists → Candidates as a batch. Nothing is added to a list until it is approved there.',
                    confirmLabel: 'Send to candidates',
                },
            })
            .afterClosed()
            .pipe(
                filter((result) => result === 'confirm'),
                switchMap(() => {
                    this.working = true;
                    this.cdr.markForCheck();
                    return this.lists.createCandidatesByChargebacks(chargebacks);
                }),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: () => {
                    this.working = false;
                    this.clearSelection();
                    this.cdr.markForCheck();
                    this.snackBar
                        .open(`${chargebacks.length} ${noun} sent to list candidates`, 'Open candidates', {
                            duration: 5000,
                        })
                        .onAction()
                        .subscribe(() => void this.router.navigate(['/lists/candidates']));
                },
                error: (error: HttpErrorResponse) => this.fail(error),
            });
    }

    private createDataSet(): void {
        const payments = this.selectedRows.map((row) => row.source as Payment);
        this.working = true;
        this.dataSets
            .saveDataSets({
                name: `${this.profile.getUsername()}_${new Date().toISOString()}`,
                rows: payments.map((payment) => ({ payment })),
            })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (id) => {
                    this.working = false;
                    void this.router.navigate(['/data-sets', id, 'testing']);
                },
                error: (error: HttpErrorResponse) => this.fail(error),
            });
    }

    private fail(error: HttpErrorResponse): void {
        this.working = false;
        this.cdr.markForCheck();
        this.snackBar.open(
            error.status === 422 ? 'The server rejected these records.' : 'Could not complete the action. Try again.',
            'OK'
        );
    }

    private apply(request: LoadRequest, page: HistoricalPage | null): void {
        this.busy = false;
        this.searched = true;
        if (!page) {
            this.failed = true;
            if (!request.continuationId) {
                this.rows = [];
                this.continuationId = undefined;
            }
        } else {
            this.rows = request.continuationId ? [...this.rows, ...page.rows] : page.rows;
            this.continuationId = page.continuationId;
        }
        this.actions.refresh();
        this.cdr.markForCheck();
    }
}
