import { NgModule } from '@angular/core';

import { GroupReferencesListComponent } from './group-references-list.component';
import { ReferenceTableComponent } from '../../../../../../shared/components/reference-table/reference-table.component';

@NgModule({
    imports: [ReferenceTableComponent],
    declarations: [GroupReferencesListComponent],
    exports: [GroupReferencesListComponent],
})
export class GroupReferencesListModule {}
