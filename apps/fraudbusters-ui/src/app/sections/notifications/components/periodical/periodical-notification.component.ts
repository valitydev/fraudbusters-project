import { ChangeDetectionStrategy, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, startWith } from 'rxjs';

import { Notification } from '../../../../api/fb-management/swagger-codegen/model/notification';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { statusPillClass } from '../../../../shared/utils/status-tone';
import { ArchiveNotificationService } from '../../services/archive-notification.service';
import { FetchNotificationsService } from '../../services/fetch-notifications.service';

/** Periodic notifications: a searchable catalog table with settings and archive actions. */
@Component({
    templateUrl: 'periodical-notification.component.html',
    styleUrls: ['../notifications-table.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchNotificationsService, ArchiveNotificationService],
    standalone: false,
})
export class PeriodicalNotificationComponent {
    readonly view$ = combineLatest({
        rows: this.fetchNotificationsService.searchResult$.pipe(startWith([])),
        busy: this.fetchNotificationsService.inProgress$,
        hasMore: this.fetchNotificationsService.hasMore$,
    });

    private searchValue = '';

    constructor(
        private router: Router,
        route: ActivatedRoute,
        private fetchNotificationsService: FetchNotificationsService,
        private archiveNotificationService: ArchiveNotificationService,
        destroyRef: DestroyRef
    ) {
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.fetchNotificationsService.search({ searchValue: query });
            });
        this.archiveNotificationService.archive$
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe(() => this.fetchNotificationsService.search({ searchValue: this.searchValue }));
    }

    pill(status?: string): string {
        return statusPillClass(status);
    }

    edit(id: number): void {
        void this.router.navigate(['/notification', id]);
    }

    archive(notification: Notification): void {
        this.archiveNotificationService.archiveNotification({ ...notification });
    }

    fetchMore(rows: Notification[]): void {
        this.fetchNotificationsService.fetchMore({
            searchValue: this.searchValue,
            lastId: String(rows[rows.length - 1].id),
        });
    }
}
