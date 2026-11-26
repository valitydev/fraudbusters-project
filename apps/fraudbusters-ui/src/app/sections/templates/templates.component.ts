import { ChangeDetectionStrategy, Component } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs/operators';

import { CatalogMetadataService } from '../../shared/components/catalog-panel/catalog-metadata.service';
import { hasActiveFragments } from '../../shared/utils/has-active-fragments';

@Component({
    templateUrl: 'templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [CatalogMetadataService],
    standalone: false,
})
export class TemplatesComponent {
    links = [
        {
            path: 'templates',
            createUrl: '/template/new',
            createLabel: 'Create template',
            name: 'Templates',
            otherActiveUrlFragments: [],
        },
        {
            path: 'references',
            createUrl: '/reference/new/payment',
            createLabel: 'Create reference',
            name: 'References',
            otherActiveUrlFragments: [],
        },
        {
            path: 'default-references',
            createUrl: '/default-references/new/payment',
            createLabel: 'Create reference',
            name: 'Default references',
            otherActiveUrlFragments: [],
        },
    ];

    readonly metadata$ = this.metadata.load('payments', 'templates');

    readonly activeTab = toSignal(
        this.router.events.pipe(
            filter((event): event is NavigationEnd => event instanceof NavigationEnd),
            map(({ urlAfterRedirects }) =>
                this.links.find((link) => '/templates/' + link.path === urlAfterRedirects.split(/[?#]/, 1)[0])
            )
        )
    );

    constructor(private router: Router, private metadata: CatalogMetadataService) {}

    get returnUrl(): string {
        return this.router.url;
    }

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }
}
