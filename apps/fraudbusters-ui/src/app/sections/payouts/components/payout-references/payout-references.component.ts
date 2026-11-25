import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map, switchMap } from 'rxjs/operators';

import { PayoutReference } from '../../../../api/fb-management/swagger-codegen/model/payoutReference';
import { PayoutsService } from '../../../../api/payouts';
import { ConfigService } from '../../../../config';
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
    templateUrl: 'payout-references.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutReferencesComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS;
    readonly catalog: CatalogCollection<ReferenceRow<PayoutReference>>;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        route: ActivatedRoute,
        config: ConfigService
    ) {
        this.catalog = new CatalogCollection<ReferenceRow<PayoutReference>>(
            route,
            config.pageSize,
            (query) =>
                loadCatalog((lastId) =>
                    this.payoutsService.findReferences({ lastId, searchValue: query, size: config.pageSize })
                ).pipe(
                    map((rows) =>
                        rows.map((source) => ({
                            ...source,
                            targetId: source.templateId,
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

    removeReference(reference: PayoutReference) {
        this.feedback
            .confirm(`Remove the reference of ${describePayoutScope(reference)} to ${reference.templateId}?`)
            .pipe(switchMap(() => this.payoutsService.removeReference(reference)))
            .subscribe({
                next: () => this.catalog.refresh(),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
