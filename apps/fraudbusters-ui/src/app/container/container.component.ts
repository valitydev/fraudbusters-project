import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, DestroyRef, ViewChild, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSidenav } from '@angular/material/sidenav';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map, shareReplay, withLatestFrom } from 'rxjs/operators';

import { FilterRolesPipe } from './components/user-info/filter-roles.pipe';
import { FormatRolePipe } from './components/user-info/format-role.pipe';
import { ProfileService } from './services/profile/profile.service';

@Component({
    selector: 'fb-container',
    templateUrl: 'container.component.html',
    styleUrls: ['container.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [ProfileService],
    standalone: false,
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

    get darkTheme(): boolean {
        return document.documentElement.classList.contains('fb-dark');
    }

    toggleTheme(): void {
        const dark = document.documentElement.classList.toggle('fb-dark');
        try {
            localStorage.setItem('fb-theme', dark ? 'dark' : 'light');
        } catch {
            // The selected theme still applies when browser storage is unavailable.
        }
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

    /** Second line of the sidebar account button, e.g. "Officer, Auditor". */
    get roleSummary(): string {
        const format = new FormatRolePipe();
        return new FilterRolesPipe()
            .transform(this.getUserRoles() ?? [])
            .map((role) => format.transform(role))
            .join(', ');
    }

    logout() {
        this.profileService.logout();
    }
}
