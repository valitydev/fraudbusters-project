import { Clipboard } from '@angular/cdk/clipboard';
import { DatePipe, NgClass, NgFor, NgIf, NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { listNamePill, ListRecordRow, ListRecordWindow, windowStatus } from './list-record-row';

/** Black/white/grey list records; grey lists add the match limit and its time window. */
@Component({
    selector: 'fb-list-records-table',
    standalone: true,
    imports: [
        DatePipe,
        NgClass,
        NgFor,
        NgIf,
        NgTemplateOutlet,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatSnackBarModule,
    ],
    templateUrl: 'list-records-table.component.html',
    styleUrls: ['list-records-table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListRecordsTableComponent<T> {
    @Input() label = 'List records';
    @Input() scope: 'Shop' | 'Wallet' = 'Shop';
    @Input() rows: readonly ListRecordRow<T>[] = [];
    /** Grey lists: show the match limit, its window and whether it is in effect now. */
    @Input() grey = false;
    @Input() showAuthor = false;

    @Output() readonly remove = new EventEmitter<T>();

    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    namePill(name?: string): string {
        return listNamePill(name);
    }

    status(window?: ListRecordWindow): { label: string; pill: string } {
        switch (windowStatus(window)) {
            case 'scheduled':
                return { label: 'Scheduled', pill: 'fb-pill-info' };
            case 'expired':
                return { label: 'Expired', pill: '' };
            default:
                return { label: 'Active', pill: 'fb-pill-success' };
        }
    }

    copy(value: string, label: string): void {
        const copied = this.clipboard.copy(value);
        this.snackBar.open(copied ? label + ' copied' : 'Could not copy ' + label, 'OK', { duration: 2500 });
    }
}
