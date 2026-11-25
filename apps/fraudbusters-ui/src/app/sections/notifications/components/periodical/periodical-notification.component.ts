import { ChangeDetectionStrategy, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';

import { Notification } from '../../../../api/fb-management/swagger-codegen/model/notification';
import { statusPillClass } from '../../../../shared/utils/status-tone';
import { ArchiveNotificationService } from '../../services/archive-notification.service';
import { FetchNotificationsService } from '../../services/fetch-notifications.service';

/** Periodic notifications: a searchable catalog table with settings and archive actions. */
@Component({
    templateUrl: 'periodical-notification.component.html',
    styleUrls: ['../notifications-table.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchNotificationsService, ArchiveNotificationService],
})
export class PeriodicalNotificationComponent {
    readonly view$ = combineLatest({
        rows: this.fetchNotificationsService.searchResult$,
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
        route.queryParamMap
            .pipe(
                map((params) => (params.get('searchQuery') ?? '').trim()),
                distinctUntilChanged(),
                takeUntilDestroyed(destroyRef)
            )
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
