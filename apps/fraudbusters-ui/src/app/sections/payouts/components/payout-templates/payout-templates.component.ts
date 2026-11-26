import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PayoutsService } from '../../../../api/payouts';
import { ConfigService } from '../../../../config';
import { createTemplateCatalog } from '../../../../shared/components/templates-table/template-catalog';
import { CATALOG_SORT_OPTIONS, loadCatalog } from '../../../../shared/utils/catalog';
import { CatalogCollection } from '../../../../shared/utils/catalog-collection';
import { PayoutsFeedbackService } from '../../services/payouts-feedback.service';

@Component({
    templateUrl: 'payout-templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class PayoutTemplatesComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS;
    readonly catalog: CatalogCollection<Template>;

    constructor(
        private payoutsService: PayoutsService,
        private feedback: PayoutsFeedbackService,
        private router: Router,
        route: ActivatedRoute,
        config: ConfigService
    ) {
        this.catalog = createTemplateCatalog(route, config.pageSize, () =>
            loadCatalog((lastId) => this.payoutsService.findTemplates({ lastId, size: config.pageSize }))
        );
    }

    editTemplate(id: string) {
        void this.router.navigate([`/payouts/template/${id}`], { queryParams: { returnUrl: this.router.url } });
    }

    copyTemplate(id: string) {
        void this.router.navigate([`/payouts/template/new/${id}`], { queryParams: { returnUrl: this.router.url } });
    }

    removeTemplate(id: string) {
        this.feedback
            .confirm(`Remove template ${id}?`)
            .pipe(switchMap(() => this.payoutsService.removeTemplate(id)))
            .subscribe({
                next: () => this.catalog.refresh(),
                error: (error: HttpErrorResponse) => this.feedback.failed(error),
            });
    }
}
