import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map, switchMap } from 'rxjs/operators';

import { PayoutGroupReference } from '../../../../api/fb-management/swagger-codegen/model/payoutGroupReference';
import { PayoutsService } from '../../../../api/payouts';
import { ConfigService } from '../../../../config';
import { CatalogMetadataService } from '../../../../shared/components/catalog-panel/catalog-metadata.service';
import { ReferenceRow } from '../../../../shared/components/reference-table/reference-table.component';
import {
    CATALOG_SORT_OPTIONS,
    catalogTimestamp,
    compareCatalogRows,
    loadCatalog,
} from '../../../../shared/utils/catalog';
import { CatalogCollection } from '../../../../shared/utils/catalog-collection';
import { describePayoutScope } from '../../services/payout-scope';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-group-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class PayoutGroupReferencesComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS;
    readonly catalog: CatalogCollection<ReferenceRow<PayoutGroupReference>>;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        route: ActivatedRoute,
        config: ConfigService,
        private metadata: CatalogMetadataService
    ) {
        this.catalog = new CatalogCollection<ReferenceRow<PayoutGroupReference>>(
            route,
            config.pageSize,
            (query) =>
                loadCatalog((lastId) =>
                    this.payoutsService.findGroupReferences({ lastId, searchValue: query, size: config.pageSize })
                ).pipe(
                    map((rows) =>
                        rows.map((source) => ({
                            ...source,
                            targetId: source.groupId,
                            scopeId: source.walletId,
                            source,
                        }))
                    )
                ),
            (a, b, sort) =>
                compareCatalogRows(
                    a.targetId,
                    b.targetId,
                    catalogTimestamp(a.lastUpdateDate),
                    catalogTimestamp(b.lastUpdateDate),
                    sort
                )
        );
    }

    removeReference(reference: PayoutGroupReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to group ${reference.groupId}?`)
            .pipe(switchMap(() => this.payoutsService.removeGroupReference(reference)))
            .subscribe({
                next: () => {
                    this.catalog.refresh();
                    this.metadata.refresh();
                },
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
