import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map, startWith, switchMap } from 'rxjs/operators';

import { PaymentListsService } from '../../api/payments/lists';
import { CatalogMetadataService } from '../../shared/components/catalog-panel/catalog-metadata.service';
import { ConfirmActionDialogComponent } from '../../shared/components/confirm-action-dialog';
import { ListsEventsService } from '../../shared/components/wb-list/services/lists-events.service';
import { ListType } from '../../shared/constants/list-type';

interface ListTab {
    path: string;
    name: string;
    listType?: ListType;
}

@Component({
    templateUrl: 'lists.component.html',
    styleUrls: ['lists.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [ListsEventsService, CatalogMetadataService],
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

    readonly metadata$ = this.metadata.load('payments', 'lists');
    cleaning = false;

    constructor(
        private router: Router,
        private listService: PaymentListsService,
        private listsEvents: ListsEventsService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private destroyRef: DestroyRef,
        private cdr: ChangeDetectorRef,
        private metadata: CatalogMetadataService
    ) {}

    ngOnInit(): void {
        this.listsEvents.changed$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.metadata.refresh());
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
