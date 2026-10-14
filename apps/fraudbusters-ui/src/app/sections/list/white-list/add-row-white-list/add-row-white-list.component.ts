import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ListType } from '../../../../shared/constants/list-type';

@Component({
    templateUrl: './add-row-white-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddRowWhiteListComponent {
    listType = ListType.White;
}
