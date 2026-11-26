import { ChangeDetectionStrategy, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest } from 'rxjs';

import { Channel } from '../../../../api/fb-management/swagger-codegen/model/channel';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { FetchChannelsService } from '../../services/fetch-channels.service';
import { RemoveChannelsService } from '../../services/remove-channels.service';

/** Notification channels: a searchable catalog table with edit and remove actions. */
@Component({
    templateUrl: 'channels.component.html',
    styleUrls: ['../notifications-table.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchChannelsService, RemoveChannelsService],
    standalone: false,
})
export class ChannelsComponent {
    readonly view$ = combineLatest({
        rows: this.fetchChannelsService.searchResult$,
        busy: this.fetchChannelsService.inProgress$,
        hasMore: this.fetchChannelsService.hasMore$,
    });

    private searchValue = '';

    constructor(
        private router: Router,
        route: ActivatedRoute,
        private fetchChannelsService: FetchChannelsService,
        private removeChannelsService: RemoveChannelsService,
        destroyRef: DestroyRef
    ) {
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.fetchChannelsService.search({ searchValue: query });
            });
        this.removeChannelsService.removed$
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe(() => this.fetchChannelsService.search({ searchValue: this.searchValue }));
    }

    edit(name: string): void {
        void this.router.navigate(['/channel', name]);
    }

    remove(channel: Channel): void {
        this.removeChannelsService.removeChannel(channel);
    }

    fetchMore(): void {
        this.fetchChannelsService.fetchMore({ searchValue: this.searchValue });
    }
}
