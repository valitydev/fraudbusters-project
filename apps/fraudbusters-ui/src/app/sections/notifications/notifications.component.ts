import { ChangeDetectionStrategy, Component } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map, startWith } from 'rxjs/operators';

interface NotificationsTab {
    path: string;
    name: string;
    createUrl: string;
    createLabel: string;
}

/** Notifications shell: title, the active tab's create action and the tab bar. */
@Component({
    templateUrl: 'notifications.component.html',
    styleUrls: ['notifications.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsComponent {
    readonly links: NotificationsTab[] = [
        { path: 'periodical', name: 'Periodical', createUrl: '/notification/new', createLabel: 'Add notification' },
        { path: 'channels', name: 'Channels', createUrl: '/channel/new', createLabel: 'Add channel' },
    ];

    readonly activeTab = toSignal(
        this.router.events.pipe(
            filter((event): event is NavigationEnd => event instanceof NavigationEnd),
            map(({ urlAfterRedirects }) => urlAfterRedirects),
            startWith(this.router.url),
            map((url) => this.links.find((link) => url.split(/[?#]/, 1)[0].endsWith('/' + link.path)))
        )
    );

    constructor(private router: Router) {}
}
