import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    templateUrl: 'payouts.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayoutsComponent {
    links = [
        { path: 'templates', name: 'Templates' },
        { path: 'references', name: 'References' },
        { path: 'groups', name: 'Groups' },
        { path: 'group-references', name: 'Group references' },
        { path: 'lists', name: 'Lists' },
    ];
}
