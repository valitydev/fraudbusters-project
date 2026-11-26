import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';

import { ConfigService } from '../../../../config';
import { CatalogMetadataService } from '../../../../shared/components/catalog-panel/catalog-metadata.service';
import { createTemplateCatalog } from '../../../../shared/components/templates-table/template-catalog';
import { CATALOG_SORT_OPTIONS } from '../../../../shared/utils/catalog';
import { FetchPaymentTemplatesService } from '../../services/fetch-payment-templates.service';
import { RemovePaymentTemplateService } from '../../services/remove-payment-template.service';

@Component({
    templateUrl: 'payment-templates.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [FetchPaymentTemplatesService, RemovePaymentTemplateService],
    standalone: false,
})
export class PaymentTemplatesComponent {
    readonly sortOptions = CATALOG_SORT_OPTIONS;
    readonly catalog = createTemplateCatalog(this.route, this.config.pageSize, () => this.fetchTemplatesService.all());

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private config: ConfigService,
        private fetchTemplatesService: FetchPaymentTemplatesService,
        private removeTemplateService: RemovePaymentTemplateService,
        snackBar: MatSnackBar,
        private metadata: CatalogMetadataService
    ) {
        this.removeTemplateService.removed$.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((id) => {
            snackBar.open(`Template ${id} has been deleted`, 'OK', { duration: 1500 });
            this.catalog.refresh();
            this.metadata.refresh();
        });
    }

    editTemplate(id: string): void {
        void this.router.navigate(['/template', id], { queryParams: { returnUrl: this.router.url } });
    }

    copyTemplate(id: string): void {
        void this.router.navigate(['/template/new', id], { queryParams: { returnUrl: this.router.url } });
    }

    removeTemplate(templateID: string): void {
        this.removeTemplateService.removeTemplate({ templateID });
    }
}
