import { Injectable, TemplateRef } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

/**
 * Lets the active tab put its buttons (Export CSV, Load fraud) into the page header, which
 * belongs to the shell component around the tabs.
 */
@Injectable()
export class HistoricalActionsService {
    readonly template$ = new BehaviorSubject<TemplateRef<unknown> | null>(null);
    /** The tab state changed: the header re-renders the actions (e.g. Export enables). */
    readonly changed$ = new Subject<void>();

    set(template: TemplateRef<unknown>): void {
        // After the tab's own change detection: setting it synchronously would change the header mid-check.
        queueMicrotask(() => this.template$.next(template));
    }

    clear(template: TemplateRef<unknown>): void {
        if (this.template$.value === template) {
            this.template$.next(null);
        }
    }

    refresh(): void {
        this.changed$.next();
    }
}
