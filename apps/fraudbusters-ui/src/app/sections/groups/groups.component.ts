import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';

import { PaymentGroupsService } from '../../api/payments/groups';
import { PaymentGroupsReferencesService } from '../../api/payments/groups-references';
import { GroupTabCounts } from '../../shared/components/groups-table/group-tab-counts';
import { SortOrder } from '../../shared/constants/sort-order';
import { hasActiveFragments } from '../../shared/utils/has-active-fragments';

@Component({
    templateUrl: 'groups.component.html',
    providers: [GroupTabCounts],
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

    readonly counts$ = this.tabCounts.load(
        () => this.groups.filter('').pipe(map(({ count, result }) => ({ count: count ?? result.length }))),
        () => this.references.filter({ size: 1, sortOrder: SortOrder.Asc })
    );

    constructor(
        private router: Router,
        private tabCounts: GroupTabCounts,
        private groups: PaymentGroupsService,
        private references: PaymentGroupsReferencesService
    ) {}

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
