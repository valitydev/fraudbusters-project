import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { startWith } from 'rxjs/operators';

import { PaymentReference } from '../../../../api/fb-management/swagger-codegen/model/paymentReference';
import { CatalogMetadataService } from '../../../../shared/components/catalog-panel/catalog-metadata.service';
import { OperationType } from '../../../../shared/constants/operation-type';
import { SortOrder } from '../../../../shared/constants/sort-order';
import { catalogSearchQuery } from '../../../../shared/utils/catalog-collection';
import { FetchReferencesService } from '../../services/fetch-references.service';
import { RemoveReferenceService } from '../../services/remove-reference.service';

@Component({
    templateUrl: 'payment-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchReferencesService, RemoveReferenceService],
    standalone: false,
})
export class PaymentReferencesComponent {
    references$ = this.fetchReferencesService.fetchResultChanges$.pipe(
        startWith({ result: [], count: 0, hasMore: false })
    );

    inProgress$ = this.fetchReferencesService.inProgress$;

    readonly sortOptions = [
        { value: SortOrder.Asc, label: 'Template ID (A–Z)' },
        { value: SortOrder.Desc, label: 'Template ID (Z–A)' },
    ];

    sort = SortOrder.Asc;
    readonly targetFragment = undefined;
    readonly tableLabel = 'References';

    private searchValue = '';

    constructor(
        route: ActivatedRoute,
        private fetchReferencesService: FetchReferencesService,
        private removeReferenceService: RemoveReferenceService,
        private metadata: CatalogMetadataService
    ) {
        const destroyRef = inject(DestroyRef);
        catalogSearchQuery(route)
            .pipe(takeUntilDestroyed(destroyRef))
            .subscribe((query) => {
                this.searchValue = query;
                this.search();
            });
        this.removeReferenceService.removed$.pipe(takeUntilDestroyed(destroyRef)).subscribe(() => {
            this.search();
            this.metadata.refresh();
        });
    }

    setSort(order: SortOrder): void {
        this.sort = order;
        this.search();
    }

    fetchMore(sortFieldValue: string): void {
        this.fetchReferencesService.fetchMore({
            sortFieldValue,
            searchValue: this.searchValue,
            sortOrder: this.sort,
            isGlobal: false,
            isDefault: false,
        });
    }

    removeReference(reference: PaymentReference) {
        this.removeReferenceService.removeReference({
            type: OperationType.Payment,
            reference,
        });
    }

    private search(): void {
        this.fetchReferencesService.search({
            searchValue: this.searchValue,
            sortOrder: this.sort,
            isGlobal: false,
            isDefault: false,
        });
    }
}
