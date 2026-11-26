import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, merge, Observable, of } from 'rxjs';
import { catchError, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { PaymentListsService } from '../../api/payments/lists';
import { ConfirmActionDialogComponent } from '../../shared/components/confirm-action-dialog';
import { ListsEventsService } from '../../shared/components/wb-list/services/lists-events.service';
import { ListType } from '../../shared/constants/list-type';
import { SortOrder } from '../../shared/constants/sort-order';

interface ListTab {
    path: string;
    name: string;
    listType?: ListType;
}

const BATCH_PAGE = 100;

@Component({
    templateUrl: 'lists.component.html',
    styleUrls: ['lists.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [ListsEventsService],
    standalone: false,
})
export class ListsComponent implements OnInit {
    readonly links: ListTab[] = [
        { path: 'black', name: 'Black', listType: ListType.Black },
        { path: 'white', name: 'White', listType: ListType.White },
        { path: 'grey', name: 'Grey', listType: ListType.Grey },
        { path: 'candidates', name: 'Candidates' },
    ];

    readonly activeTab = toSignal(
        this.router.events.pipe(
            filter((event): event is NavigationEnd => event instanceof NavigationEnd),
            map(({ urlAfterRedirects }) => urlAfterRedirects),
            startWith(this.router.url),
            map((url) => this.links.find((link) => url.split(/[?#]/, 1)[0].startsWith('/lists/' + link.path)))
        )
    );

    /** Record count per tab; candidates show loaded batches ("100+" when there are more). */
    counts$: Observable<Record<string, string>>;
    cleaning = false;

    private readonly reloadCounts$ = new BehaviorSubject<void>(undefined);

    constructor(
        private router: Router,
        private listService: PaymentListsService,
        private listsEvents: ListsEventsService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        const count = (listType: ListType) =>
            // The backend counts only the list names it is given, so ask for every name of the type.
            this.listService.getNames(listType).pipe(
                switchMap((listNames) =>
                    listNames.length
                        ? this.listService
                              .findListRows({ listType, listNames, size: 1, sortOrder: SortOrder.Desc })
                              .pipe(map(({ count: total }) => String(total ?? 0)))
                        : of('0')
                ),
                catchError(() => of(''))
            );
        const batches = this.listService.findCandidatesBatch({ size: BATCH_PAGE, sortOrder: SortOrder.Desc }).pipe(
            map(({ result, lastId }) =>
                result.length >= BATCH_PAGE && lastId ? `${result.length}+` : String(result.length)
            ),
            catchError(() => of(''))
        );
        this.counts$ = merge(this.reloadCounts$, this.listsEvents.changed$).pipe(
            switchMap(() =>
                combineLatest([count(ListType.Black), count(ListType.White), count(ListType.Grey), batches])
            ),
            map(([black, white, grey, candidates]) => ({ black, white, grey, candidates })),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        this.router.events
            .pipe(
                filter((event) => event instanceof NavigationEnd),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(() => this.reloadCounts$.next());
    }

    get returnUrl(): string {
        return this.router.url;
    }

    cleanByCsv(input: HTMLInputElement): void {
        const file = input.files?.[0];
        input.value = '';
        const tab = this.activeTab();
        if (!file || !tab?.listType) {
            return;
        }
        if (!/\.csv$/i.test(file.name)) {
            this.snackBar.open('Choose a CSV file', 'OK', { duration: 2500 });
            return;
        }
        this.dialog
            .open(ConfirmActionDialogComponent, {
                data: {
                    title: `Remove records listed in ${file.name} from the ${tab.name} list?`,
                    message: 'Matching records stop applying to rules right away and cannot be restored.',
                    confirmLabel: 'Remove',
                    destructive: true,
                },
            })
            .afterClosed()
            .pipe(
                filter((result) => result === 'confirm'),
                switchMap(() => {
                    this.cleaning = true;
                    return this.listService.deleteListsRowsByFile(tab.listType, file);
                }),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: () => {
                    this.cleaning = false;
                    this.cdr.markForCheck();
                    this.snackBar.open(`Records from ${file.name} removed`, 'OK', { duration: 3000 });
                    this.listsEvents.changed(tab.listType);
                },
                error: (error: HttpErrorResponse) => {
                    this.cleaning = false;
                    this.cdr.markForCheck();
                    this.snackBar.open(`${error.status}: ${error.message}`, 'OK');
                },
            });
    }
}
