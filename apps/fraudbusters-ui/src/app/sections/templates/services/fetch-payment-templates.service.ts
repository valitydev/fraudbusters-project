import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Template } from '../../../api/fb-management/swagger-codegen/model/template';
import { PaymentTemplatesService } from '../../../api/payments/templates';
import { ConfigService } from '../../../config';
import { SortOrder } from '../../../shared/constants/sort-order';
import { loadCatalog } from '../../../shared/utils/catalog';

@Injectable()
export class FetchPaymentTemplatesService {
    constructor(private api: PaymentTemplatesService, private config: ConfigService) {}

    all(): Observable<Template[]> {
        return loadCatalog((lastId) =>
            this.api.findTemplates({
                size: this.config.pageSize,
                sortOrder: SortOrder.Asc,
                lastId,
            })
        );
    }
}
