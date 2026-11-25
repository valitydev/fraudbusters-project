import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
    selector: 'fb-user-info',
    templateUrl: 'user-info.component.html',
    styleUrls: ['user-info.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserInfoComponent {
    @Input() username: string;
    @Input() roles: string[] = [];

    get initial(): string {
        return this.username?.trim().charAt(0).toUpperCase() || '?';
    }
}
