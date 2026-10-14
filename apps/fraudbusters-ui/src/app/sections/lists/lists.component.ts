import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { Router } from '@angular/router';

import { hasActiveFragments } from '../../shared/utils/has-active-fragments';
import { LAYOUT_GAP_S } from '../../tokens';

@Component({
    templateUrl: 'lists.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListsComponent {
    links = [
        {
            path: 'black',
            name: 'Black',
            otherActiveUrlFragments: [],
        },
        {
            path: 'white',
            name: 'White',
            otherActiveUrlFragments: [],
        },
        {
            path: 'grey',
            name: 'Grey',
            otherActiveUrlFragments: [],
        },
        {
            path: 'candidates',
            name: 'Candidates',
            otherActiveUrlFragments: [],
        },
    ];

    constructor(private router: Router, @Inject(LAYOUT_GAP_S) public layoutGapS: string) {}

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }
}
