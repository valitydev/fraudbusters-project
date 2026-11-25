import { Clipboard } from '@angular/cdk/clipboard';
import { DatePipe, NgClass, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';

import { HistoricalColumn, HistoricalKind } from './historical-kinds';
import { formatAmount, HistoricalRow } from './historical-row';
import { statusPillClass } from '../../../shared/utils/status-tone';

const COLUMN_LABELS: Record<HistoricalColumn, string> = {
    status: 'Status',
    amount: 'Amount',
    party: 'Party ID',
    scope: 'Shop ID',
    email: 'Email',
    user: 'User ID',
    method: 'Method / BIN',
    category: 'Category',
    code: 'Code',
    fraudType: 'Fraud type',
    template: 'Template',
    rule: 'Rule',
};

/** Historical records of one tab; a row opens its details, and may be selected for a bulk action. */
@Component({
    selector: 'fb-historical-table',
    standalone: true,
    imports: [
        DatePipe,
        NgClass,
        NgFor,
        NgIf,
        NgSwitch,
        NgSwitchCase,
        NgSwitchDefault,
        RouterLink,
        MatCheckboxModule,
        MatIconModule,
        MatSnackBarModule,
    ],
    templateUrl: 'historical-table.component.html',
    styleUrls: ['historical-table.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoricalTableComponent implements OnChanges {
    @Input({ required: true }) kind: HistoricalKind;
    @Input() rows: readonly HistoricalRow[] = [];
    /** Keys of the selected rows; no checkboxes without a bulk action. */
    @Input() selected: ReadonlySet<string> | null = null;

    @Output() readonly toggle = new EventEmitter<HistoricalRow>();
    @Output() readonly toggleAll = new EventEmitter<boolean>();

    expanded = new Set<string>();

    constructor(private clipboard: Clipboard, private snackBar: MatSnackBar) {}

    get selectable(): boolean {
        return this.selected !== null;
    }

    get allSelected(): boolean {
        return this.rows.length > 0 && this.rows.every((row) => this.selected?.has(row.key));
    }

    get someSelected(): boolean {
        return !this.allSelected && this.rows.some((row) => this.selected?.has(row.key));
    }

    /** Columns plus ID, event time, the details chevron and the checkbox. */
    get columnCount(): number {
        return this.kind.columns.length + 3 + Number(this.selectable);
    }

    ngOnChanges(): void {
        const keys = new Set(this.rows.map((row) => row.key));
        this.expanded = new Set([...this.expanded].filter((key) => keys.has(key)));
    }

    label(column: HistoricalColumn): string {
        if (column === 'status') {
            return this.kind.statusLabel;
        }
        if (column === 'scope' && this.kind.operation === 'payout') {
            return 'Wallet ID';
        }
        if (column === 'method' && this.kind.operation === 'payout') {
            return 'Destination';
        }
        return COLUMN_LABELS[column];
    }

    pill(status?: string): string {
        return statusPillClass(status);
    }

    amount(row: HistoricalRow): string {
        return formatAmount(row.amount);
    }

    expand(row: HistoricalRow, event?: Event): void {
        // Links, buttons and checkboxes inside the row keep their own behaviour.
        if ((event?.target as HTMLElement)?.closest('a, button, input, mat-checkbox')) {
            return;
        }
        if (!this.expanded.delete(row.key)) {
            this.expanded.add(row.key);
        }
        this.expanded = new Set(this.expanded);
    }

    copy(value: string, what: string): void {
        const copied = this.clipboard.copy(value);
        this.snackBar.open(copied ? `${what} copied` : `Could not copy ${what}`, 'OK', { duration: 2500 });
    }

    trackRow(_: number, row: HistoricalRow): string {
        return row.key;
    }
}
