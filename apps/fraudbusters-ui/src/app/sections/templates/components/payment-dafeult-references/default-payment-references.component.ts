import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { startWith } from 'rxjs/operators';

import { PaymentReference } from '../../../../api/fb-management/swagger-codegen/model/paymentReference';
import { OperationType } from '../../../../shared/constants/operation-type';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { FetchDefaultReferencesService } from '../../services/fetch-default-references.service';
import { RemoveDefaultReferenceService } from '../../services/remove-default-reference.service';

@Component({
    templateUrl: '../payment-references/payment-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchDefaultReferencesService, RemoveDefaultReferenceService],
    standalone: false,
})
export class DefaultPaymentReferencesComponent {
    references$ = this.fetchDefaultReferencesService.fetchResultChanges$.pipe(
        startWith({ result: [], count: 0, hasMore: false })
    );

    inProgress$ = this.fetchDefaultReferencesService.inProgress$;

    readonly sortOptions = [
        { value: SortOrder.Asc, label: 'Template ID (A–Z)' },
        { value: SortOrder.Desc, label: 'Template ID (Z–A)' },
    ];

    sort = SortOrder.Asc;
    readonly targetFragment = OperationType.Payment;
    readonly tableLabel = 'Default references';

    private searchValue = '';

    constructor(
        route: ActivatedRoute,
        private fetchDefaultReferencesService: FetchDefaultReferencesService,
        private removeDefaultReferenceService: RemoveDefaultReferenceService
    ) {
        const destroyRef = inject(DestroyRef);
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.search();
            });
        this.removeDefaultReferenceService.removed$.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => {
            this.search();
        });
    }

    setSort(order: SortOrder): void {
        this.sort = order;
        this.search();
    }

    fetchMore(sortFieldValue: string): void {
        this.fetchDefaultReferencesService.fetchMore({
            sortFieldValue,
            searchValue: this.searchValue,
            sortOrder: this.sort,
            isGlobal: false,
            isDefault: false,
        });
    }

    removeReference(reference: PaymentReference) {
        this.removeDefaultReferenceService.removeReference({
            reference,
        });
    }

    private search(): void {
        this.fetchDefaultReferencesService.search({
            searchValue: this.searchValue,
            sortOrder: this.sort,
            isGlobal: false,
            isDefault: false,
        });
    }
}
