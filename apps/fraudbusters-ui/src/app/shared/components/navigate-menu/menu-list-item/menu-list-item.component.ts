import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    inject,
    Input,
    OnInit,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { filter } from 'rxjs/operators';

import { NavItem } from '../../../model/nav-item';

@Component({
    selector: 'fb-menu-list-item',
    templateUrl: './menu-list-item.component.html',
    styleUrls: ['./menu-list-item.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MenuListItemComponent implements OnInit {
    @Input() item: NavItem;
    @Input() depth = 0;

    expanded = false;

    private destroyRef = inject(DestroyRef);

    constructor(public router: Router, protected readonly keycloak: KeycloakService, private cdr: ChangeDetectorRef) {}

    /** A group opens when one of its pages is opened, and stays as the user leaves it otherwise. */
    ngOnInit() {
        if (!this.hasChildren()) {
            return;
        }
        this.expanded = this.containsCurrentPage(this.item);
        this.router.events
            .pipe(
                filter((event) => event instanceof NavigationEnd),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(() => {
                if (this.containsCurrentPage(this.item)) {
                    this.expanded = true;
                    this.cdr.markForCheck();
                }
            });
    }

    hasChildren(): boolean {
        return !!this.item.children?.length;
    }

    /** A group is shown when the user may open at least one of its items. */
    display(): boolean {
        return this.isAllowed(this.item);
    }

    toggle() {
        this.expanded = !this.expanded;
    }

    private isAllowed(item: NavItem): boolean {
        if (item.children?.length) {
            return item.children.some((child) => this.isAllowed(child));
        }
        return item.roles.some((role) => this.keycloak.getUserRoles().includes(role));
    }

    /** A group contains its items' pages and, when it has its own route, every page under that route. */
    private containsCurrentPage(item: NavItem): boolean {
        if (item.children?.length) {
            return this.isUnder(item.route) || item.children.some((child) => this.containsCurrentPage(child));
        }
        return this.isUnder(item.route);
    }

    private isUnder(route?: string): boolean {
        if (!route) {
            return false;
        }
        const path = `/${route}`;
        return this.router.url === path || this.router.url.startsWith(`${path}/`);
    }
}
