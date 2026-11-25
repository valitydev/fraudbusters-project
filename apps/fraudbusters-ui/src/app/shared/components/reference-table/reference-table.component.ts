import { Clipboard } from '@angular/cdk/clipboard';
import { DatePipe, NgFor, NgIf, NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';

export interface ReferenceRow<T> {
    id?: string;
    targetId?: string;
    partyId?: string;
    scopeId?: string;
    lastUpdateDate?: Date;
    modifiedByUser?: string;
    isGlobal?: boolean;
    source: T;
}

@Component({
    selector: 'fb-reference-table',
    standalone: true,
    imports: [
        DatePipe,
        NgFor,
        NgIf,
        NgTemplateOutlet,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatSnackBarModule,
        RouterLink,
    ],
    templateUrl: 'reference-table.component.html',
    styleUrls: ['reference-table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReferenceTableComponent<T> {
    @Input() label: string;
    @Input() target: 'Template' | 'Group' = 'Template';
    @Input({ required: true }) targetRoute: string;
    @Input() targetFragment?: string;
    @Input() scope: 'Shop' | 'Wallet' = 'Shop';
    @Input() rows: readonly ReferenceRow<T>[] = [];
    @Input() showGlobal = false;
    @Input() showAuthor = false;

    @Output() readonly remove = new EventEmitter<T>();

    constructor(private router: Router, private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    get returnUrl(): string {
        return this.router.url;
    }

    copyId(id: string, label: string): void {
        const copied = this.clipboard.copy(id);
        this.snackBar.open(copied ? label + ' copied' : 'Could not copy ' + label, 'OK', { duration: 2500 });
    }
}
