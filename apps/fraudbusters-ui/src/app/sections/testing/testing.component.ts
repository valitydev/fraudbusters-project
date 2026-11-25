import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { hasActiveFragments } from '../../shared/utils/has-active-fragments';

@Component({
    templateUrl: 'testing.component.html',
})
export class TestingComponent {
    links = [
        {
            path: 'data-sets',
            name: 'Data sets',
            otherActiveUrlFragments: [],
        },
        {
            path: 'emulation',
            name: 'Emulation',
            otherActiveUrlFragments: [],
        },
    ];

    constructor(private router: Router) {}

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }
}
