import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, Observable, combineLatest, of } from 'rxjs';
import { catchError, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutTemplatesComponent } from './components/payout-templates/payout-templates.component';
import { ListType } from '../../api/fb-management/swagger-codegen/model/listType';
import { PayoutsService } from '../../api/payouts';
import { GroupTabCounts } from '../../shared/components/groups-table/group-tab-counts';
import { SortOrder } from '../../shared/constants/sort-order';

export interface PayoutSectionLink {
    path: string;
    name: string;
    listType?: ListType;
    createUrl?: string;
    createLabel?: string;
}

/** A payout section with its own tabs, laid out like the payment section of the same name. */
@Component({
    templateUrl: 'payout-section.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [GroupTabCounts],
    standalone: false,
})
export class PayoutSectionComponent {
    readonly view$ = combineLatest([
        this.route.data,
        this.router.events.pipe(
            filter((event) => event instanceof NavigationEnd),
            startWith(null)
        ),
    ]).pipe(
        map(([data]) => {
            const links = data.links as PayoutSectionLink[];
            const url = this.router.url;
            const path = url.split(/[?#]/, 1)[0].split('/').pop();
            return {
                links,
                title: data.title as string,
                subtitle: data.subtitle as string,
                action: links.find((link) => link.path === path),
                url,
            };
        })
    );

    templateCatalog?: PayoutTemplatesComponent['catalog'];
    readonly groupCounts$ = this.groupTabs.load(
        () => this.payouts.findGroups(),
        () => this.payouts.findGroupReferences({ size: 1, sortOrder: SortOrder.Asc })
    );

    /** Templates and references totals, loaded with the page and again after each navigation. */
    readonly templateCounts$: Observable<Record<string, number | null>> = this.router.events.pipe(
        filter((event) => event instanceof NavigationEnd),
        startWith(null),
        switchMap(() =>
            combineLatest({
                templates: this.count(this.payouts.findTemplates({ size: 1 })),
                references: this.count(this.payouts.findReferences({ size: 1 })),
            })
        ),
        shareReplay({ bufferSize: 1, refCount: true })
    );

    readonly listCounts$: Observable<Record<string, string>>;
    private readonly listChanges = new BehaviorSubject<Observable<void> | null>(null);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private payouts: PayoutsService,
        private groupTabs: GroupTabCounts
    ) {
        const listTabs = (this.route.snapshot.data.links as PayoutSectionLink[]).filter((link) => link.listType);
        this.listCounts$ = this.listChanges.pipe(
            switchMap((changes) => changes ?? of()),
            startWith(undefined),
            switchMap(() =>
                listTabs.length
                    ? combineLatest(
                          listTabs.map(({ path, listType }) =>
                              this.payouts.findListRows({ listType, size: 1 }).pipe(
                                  map(({ count, result }): [string, string] => [path, String(count ?? result.length)]),
                                  catchError(() => of<[string, string]>([path, '']))
                              )
                          )
                      ).pipe(map((entries) => Object.fromEntries<string>(entries)))
                    : of({})
            ),
            shareReplay({ bufferSize: 1, refCount: true })
        );
    }

    setActiveList(component: unknown): void {
        this.listChanges.next(component instanceof PayoutListsComponent ? component.removed : null);
        this.templateCatalog = component instanceof PayoutTemplatesComponent ? component.catalog : undefined;
    }

    private count(request: Observable<{ count?: number }>): Observable<number | null> {
        return request.pipe(
            map(({ count }) => count ?? null),
            // A failed count only hides the badge; the tab reports its own errors.
            catchError(() => of(null))
        );
    }
}
