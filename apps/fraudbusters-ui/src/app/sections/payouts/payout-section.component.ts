import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { filter, map, startWith } from 'rxjs/operators';

import { ListType } from '../../api/fb-management/swagger-codegen/model/listType';
import { CatalogMetadataService, CatalogSection } from '../../shared/components/catalog-panel/catalog-metadata.service';

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
    providers: [CatalogMetadataService],
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

    readonly metadata$ = this.metadata.load('payouts', this.route.snapshot.data.catalogSection as CatalogSection);

    constructor(private route: ActivatedRoute, private router: Router, private metadata: CatalogMetadataService) {}
}
