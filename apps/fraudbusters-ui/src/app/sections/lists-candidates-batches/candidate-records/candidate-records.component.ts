import { NgClass, NgFor, NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, map, switchMap } from 'rxjs/operators';

import {
    CANDIDATE_LIST_LABELS,
    CandidateListType,
    CandidateRecordRow,
    candidateRecordRow,
    matchesCandidate,
} from './candidate-record';
import { PaymentListsService } from '../../../api/payments/lists';
import { CatalogPanelComponent } from '../../../shared/components/catalog-panel/catalog-panel.component';
import {
    ConfirmActionDialogComponent,
    ConfirmActionDialogModule,
} from '../../../shared/components/confirm-action-dialog';
import { listNamePill } from '../../../shared/components/list-records-table/list-record-row';
import { SortOrder } from '../../../shared/constants/sort-order';

/** Candidates have no paging total; one batch is loaded up to this size. */
export const CANDIDATE_RECORDS_LIMIT = 1000;

/**
 * Records of one candidate batch: filter them, pick the confirmed ones and add them to the list
 * each record is proposed for. Records without a value cannot be picked.
 */
@Component({
    standalone: true,
    imports: [
        NgClass,
        NgFor,
        NgIf,
        MatButtonModule,
        MatCheckboxModule,
        MatDialogModule,
        MatIconModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTooltipModule,
        CatalogPanelComponent,
        ConfirmActionDialogModule,
    ],
    templateUrl: './candidate-records.component.html',
    styleUrls: [
        '../../lists/lists.component.scss',
        '../../../shared/components/list-records-table/list-records-table.component.scss',
        './candidate-records.component.scss',
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CandidateRecordsComponent {
    readonly labels = CANDIDATE_LIST_LABELS;
    readonly listTypes = Object.keys(CANDIDATE_LIST_LABELS) as CandidateListType[];
    readonly batchId: string;

    readonly rows = signal<CandidateRecordRow[]>([]);
    readonly source = signal('');
    readonly busy = signal(true);
    readonly failed = signal(false);
    readonly approving = signal(false);
    readonly listType = signal<CandidateListType | ''>('');
    readonly listName = signal('');
    readonly search = signal('');
    readonly selected = signal<ReadonlySet<string>>(new Set());

    readonly listNames = computed(() =>
        [
            ...new Set(
                this.rows()
                    .map((row) => row.listName)
                    .filter(Boolean)
            ),
        ].sort()
    );
    readonly visible = computed(() =>
        this.rows().filter(
            (row) =>
                (!this.listType() || row.listType === this.listType()) &&
                (!this.listName() || row.listName === this.listName()) &&
                matchesCandidate(row, this.search())
        )
    );

    readonly selectable = computed(() => this.visible().filter((row) => !row.issue));
    readonly selectedRows = computed(() => this.rows().filter((row) => this.selected().has(row.id)));
    readonly issues = computed(() => this.rows().filter((row) => row.issue).length);
    readonly allSelected = computed(
        () => this.selectable().length > 0 && this.selectable().every((row) => this.selected().has(row.id))
    );

    readonly someSelected = computed(
        () => !this.allSelected() && this.selectable().some((row) => this.selected().has(row.id))
    );

    /** "2 to Black, 1 to White" for the selected records. */
    readonly breakdown = computed(() => {
        const counts = new Map<CandidateListType, number>();
        for (const row of this.selectedRows()) {
            counts.set(row.listType, (counts.get(row.listType) ?? 0) + 1);
        }
        return this.listTypes
            .filter((type) => counts.has(type))
            .map((type) => `${counts.get(type)} to ${this.labels[type]}`)
            .join(', ');
    });

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private listService: PaymentListsService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef
    ) {
        this.batchId = route.snapshot.paramMap.get('id') ?? '';
        route.queryParamMap
            .pipe(
                map((params) => params.get('searchQuery') ?? ''),
                takeUntilDestroyed(destroyRef)
            )
            .subscribe((query) => this.search.set(query));
        this.load();
    }

    load(): void {
        this.busy.set(true);
        this.failed.set(false);
        // The backend matches searchValue against the batch ID too, which is how one batch is loaded.
        this.listService
            .findCandidates({ searchValue: this.batchId, sortOrder: SortOrder.Desc, size: CANDIDATE_RECORDS_LIMIT })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: ({ result, source }) => {
                    const rows = (result ?? []).map(candidateRecordRow);
                    this.rows.set(rows);
                    this.source.set(source ?? '');
                    const ids = new Set(rows.filter((row) => !row.issue).map((row) => row.id));
                    this.selected.update((selected) => new Set([...selected].filter((id) => ids.has(id))));
                    this.busy.set(false);
                },
                error: () => {
                    this.failed.set(true);
                    this.busy.set(false);
                },
            });
    }

    back(): void {
        void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || '/lists/candidates');
    }

    setListType(type: string): void {
        this.listType.set(type as CandidateListType | '');
    }

    toggle(row: CandidateRecordRow): void {
        if (row.issue) {
            return;
        }
        this.selected.update((selected) => {
            const next = new Set(selected);
            if (!next.delete(row.id)) {
                next.add(row.id);
            }
            return next;
        });
    }

    /** Selects or clears every record the current filters show; other selections stay. */
    toggleAll(): void {
        const ids = this.selectable().map((row) => row.id);
        const select = !this.allSelected();
        this.selected.update((selected) => {
            const next = new Set(selected);
            ids.forEach((id) => (select ? next.add(id) : next.delete(id)));
            return next;
        });
    }

    clearSelection(): void {
        this.selected.set(new Set());
    }

    namePill(name: string): string {
        return listNamePill(name);
    }

    listDot(type?: CandidateListType): string {
        return 'list-dot-' + (type ?? 'unknown');
    }

    approve(): void {
        const rows = this.selectedRows();
        if (!rows.length || this.approving()) {
            return;
        }
        const noun = rows.length === 1 ? 'record' : 'records';
        this.dialog
            .open(ConfirmActionDialogComponent, {
                data: {
                    title: `Add ${rows.length} ${noun} to lists?`,
                    message: `${this.breakdown()}. Added records start applying to rules right away.`,
                    confirmLabel: 'Add to list',
                },
            })
            .afterClosed()
            .pipe(
                filter((result) => result === 'confirm'),
                switchMap(() => {
                    this.approving.set(true);
                    return this.listService.approveCandidates(rows.map((row) => row.id));
                }),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: () => {
                    this.approving.set(false);
                    this.snackBar.open(`${rows.length} ${noun} added to lists`, 'OK', { duration: 3000 });
                    this.clearSelection();
                    this.load();
                },
                error: ({ status }: HttpErrorResponse) => {
                    this.approving.set(false);
                    this.snackBar.open(
                        status === 422 ? 'The server rejected these records.' : 'Could not add the records. Try again.',
                        'OK'
                    );
                },
            });
    }
}
