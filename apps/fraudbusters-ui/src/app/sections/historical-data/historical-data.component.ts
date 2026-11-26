import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { merge } from 'rxjs';

import { HistoricalActionsService } from './records/historical-actions.service';

export interface HistoricalDataLink {
    path: string;
    name: string;
    /** Colour of the dot before the tab name. */
    dot: string;
}

/** Page shell: title, the active tab's header actions and the tab bar. */
@Component({
    templateUrl: 'historical-data.component.html',
    styleUrls: ['historical-data.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [HistoricalActionsService],
    standalone: false,
})
export class HistoricalDataComponent {
    readonly title: string;
    readonly subtitle: string;
    readonly links: HistoricalDataLink[];

    constructor(
        route: ActivatedRoute,
        readonly actions: HistoricalActionsService,
        cdr: ChangeDetectorRef,
        destroyRef: DestroyRef
    ) {
        const data = route.snapshot.data;
        this.title = data.title;
        this.subtitle = data.subtitle;
        this.links = data.links;
        merge(actions.template$, actions.changed$)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe(() => cdr.markForCheck());
    }
}
