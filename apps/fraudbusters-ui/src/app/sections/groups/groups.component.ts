import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { CatalogMetadataService } from '../../shared/components/catalog-panel/catalog-metadata.service';
import { hasActiveFragments } from '../../shared/utils/has-active-fragments';

@Component({
    templateUrl: 'groups.component.html',
    providers: [CatalogMetadataService],
    standalone: false,
})
export class GroupsComponent {
    links = [
        {
            path: 'list',
            createUrl: '/group/new',
            createLabel: 'Create group',
            name: 'Groups',
            otherActiveUrlFragments: [],
        },
        {
            path: 'references',
            createUrl: '/create-group-reference/payment',
            createLabel: 'Create reference',
            name: 'References',
            otherActiveUrlFragments: [],
        },
    ];

    readonly metadata$ = this.metadata.load('payments', 'groups');

    constructor(private router: Router, private metadata: CatalogMetadataService) {}

    get activeTab() {
        const path = this.router.url.split(/[?#]/, 1)[0];
        return this.links.find((link) => path === '/groups/' + link.path);
    }

    get returnUrl(): string {
        return this.router.url;
    }

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }
}
