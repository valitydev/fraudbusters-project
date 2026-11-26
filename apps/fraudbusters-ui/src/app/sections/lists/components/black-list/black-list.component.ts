import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ListType } from '../../../../shared/constants/list-type';

@Component({
    templateUrl: './black-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class BlackListComponent {
    listType = ListType.Black;
}
