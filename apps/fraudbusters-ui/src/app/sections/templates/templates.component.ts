import { ChangeDetectionStrategy, Component } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, filter, map, shareReplay, startWith, switchMap } from 'rxjs/operators';

import { PaymentTemplatesService } from '../../api';
import { PaymentTemplatesComponent } from './components/payment-templates/payment-templates.component';
import { PaymentDefaultReferencesService } from '../../api/payments/default-references';
import { PaymentReferencesService } from '../../api/payments/references';
import { SortOrder } from '../../shared/constants/sort-order';
import { hasActiveFragments } from '../../shared/utils/has-active-fragments';

const COUNT_QUERY = { size: 1, searchValue: '', sortFieldValue: '', sortOrder: SortOrder.Asc, sortBy: 'template_id' };

@Component({
    templateUrl: 'templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
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

    templateCatalog?: PaymentTemplatesComponent['catalog'];

    /**
     * Totals for every tab, loaded with the page (one size-1 request each, only the count is used)
     * and again after each navigation, e.g. coming back from a create form.
     */
    readonly counts$: Observable<Record<string, number | null>> = this.router.events.pipe(
        filter((event) => event instanceof NavigationEnd),
        startWith(null),
        switchMap(() =>
            combineLatest({
                templates: this.count(this.templates.findTemplates({ size: 1, sortOrder: SortOrder.Asc })),
                references: this.count(
                    this.references.findReferences({ ...COUNT_QUERY, isGlobal: false, isDefault: false })
                ),
                'default-references': this.count(this.defaultReferences.filter(COUNT_QUERY)),
            })
        ),
        shareReplay({ bufferSize: 1, refCount: true })
    );

    readonly activeTab = toSignal(
        this.router.events.pipe(
            filter((event): event is NavigationEnd => event instanceof NavigationEnd),
            map(({ urlAfterRedirects }) =>
                this.links.find((link) => '/templates/' + link.path === urlAfterRedirects.split(/[?#]/, 1)[0])
            )
        )
    );

    constructor(
        private router: Router,
        private templates: PaymentTemplatesService,
        private references: PaymentReferencesService,
        private defaultReferences: PaymentDefaultReferencesService
    ) {}

    get returnUrl(): string {
        return this.router.url;
    }

    hasActiveFragments(fragments: string[]): boolean {
        const ulrFragments = this.router.url.split('/');
        return hasActiveFragments(fragments, ulrFragments);
    }

    setTemplateCatalog(component: unknown): void {
        this.templateCatalog = component instanceof PaymentTemplatesComponent ? component.catalog : undefined;
    }

    private count(request: Observable<{ count?: number }>): Observable<number | null> {
        return request.pipe(
            map(({ count }) => count ?? null),
            // A failed count only hides the badge; the tab itself reports its own errors.
            catchError(() => of(null))
        );
    }
}
