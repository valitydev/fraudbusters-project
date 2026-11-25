import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { Template } from '../../../api/fb-management/swagger-codegen/model/template';
import { catalogTimestamp, compareCatalogRows } from '../../utils/catalog';
import { CatalogCollection } from '../../utils/catalog-collection';

export function createTemplateCatalog(route: ActivatedRoute, pageSize: number, load: () => Observable<Template[]>) {
    return new CatalogCollection(
        route,
        pageSize,
        load,
        (a, b, sort) =>
            compareCatalogRows(
                a.id,
                b.id,
                catalogTimestamp(a.lastUpdateDate),
                catalogTimestamp(b.lastUpdateDate),
                sort
            ),
        (row, term) => row.id?.toLowerCase().includes(term) || row.template?.toLowerCase().includes(term)
    );
}
