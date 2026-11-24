import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, DestroyRef, ViewChild, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSidenav } from '@angular/material/sidenav';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map, shareReplay, withLatestFrom } from 'rxjs/operators';

import { ProfileService } from './services/profile/profile.service';

@Component({
    selector: 'fb-container',
    templateUrl: 'container.component.html',
    styleUrls: ['container.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [ProfileService],
})
export class ContainerComponent {
    readonly narrow$ = inject(BreakpointObserver)
        .observe('(max-width: 700px)')
        .pipe(
            map(({ matches }) => matches),
            shareReplay({ bufferSize: 1, refCount: true })
        );

    @ViewChild(MatSidenav) private sidenav?: MatSidenav;

    constructor(private profileService: ProfileService, router: Router) {
        router.events
            .pipe(
                filter((event) => event instanceof NavigationEnd),
                withLatestFrom(this.narrow$),
                takeUntilDestroyed(inject(DestroyRef))
            )
            .subscribe(([, narrow]) => {
                if (narrow) {
                    this.sidenav?.close();
                }
            });
    }

    get userInitial(): string {
        return this.getUsername()?.trim().charAt(0).toUpperCase() || '?';
    }

    getUsername(): string {
        return this.profileService.getUsername();
    }

    getUserRoles(): string[] {
        return this.profileService.getUserRoles();
    }

    logout() {
        this.profileService.logout();
    }
}
