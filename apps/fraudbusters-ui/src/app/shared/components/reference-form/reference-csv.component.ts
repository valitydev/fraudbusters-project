import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { Papa } from 'ngx-papaparse';

import { normalizeReference, ReferenceDraft, ReferenceScope, ReferenceTarget, TARGET_LABELS } from './reference-draft';
import { CsvPanelComponent, CsvParser } from '../csv-panel/csv-panel.component';

@Component({
    selector: 'fb-reference-csv',
    standalone: true,
    imports: [CsvPanelComponent],
    templateUrl: 'reference-csv.component.html',
})
export class ReferenceCsvComponent {
    @Input({ required: true }) scope: ReferenceScope;
    @Input() target: ReferenceTarget = 'template';

    /** Rows may leave the party empty (or set is_global) to apply to every operation. */
    @Input() partyOptional = false;

    @Output() readonly append = new EventEmitter<ReferenceDraft[]>();
    @Output() readonly closed = new EventEmitter<void>();

    @ViewChild(CsvPanelComponent) readonly panel: CsvPanelComponent<ReferenceDraft>;

    constructor(private papa: Papa) {}

    /** Reads target, party and scope columns; a header row is optional. */
    readonly parse: CsvParser<ReferenceDraft> = (text, label) => {
        const parsed = this.papa.parse(text, {
            skipEmptyLines: 'greedy',
            delimitersToGuess: [',', ';', '\t'],
        });
        const cells: string[][] = parsed.data;
        const header = (cells[0] ?? []).map((cell) =>
            cell
                .trim()
                .toLowerCase()
                .replace(/[_\s-]/g, '')
        );
        const targetColumn = header.findIndex((name) => name === this.target || name === this.target + 'id');
        const party = header.indexOf('partyid');
        const scope = header.indexOf(this.scope + 'id');
        const otherScope = header.indexOf(this.isPayout ? 'shopid' : 'walletid');
        const global = header.findIndex((name) => name === 'isglobal' || name === 'global');
        const hasHeader = targetColumn !== -1 && (party !== -1 || global !== -1 || scope !== -1 || otherScope !== -1);
        if (hasHeader && (otherScope !== -1 || (!this.isPayout && global !== -1))) {
            return { rows: [], issue: `${label}: this form uses ${this.scopeLabel} as the scope column.` };
        }

        const invalid = new Set(parsed.errors.filter(({ type }) => type === 'Quotes').map(({ row }) => row));
        const rows: ReferenceDraft[] = [];
        let skipped = 0;
        cells.forEach((columns, index) => {
            if (hasHeader && index === 0) {
                return;
            }
            const flag = this.isPayout ? (columns[hasHeader ? global : 3] ?? '').trim().toLowerCase() : '';
            // A global row is only valid where the form allows an empty party: it then binds every operation.
            const globalRow = this.partyOptional && /^(true|1|yes)$/.test(flag);
            const validFlag = globalRow || /^(false|0|no)?$/.test(flag);
            const row = normalizeReference(
                columns[hasHeader ? targetColumn : 0] ?? '',
                globalRow ? '' : columns[hasHeader ? party : 1] ?? '',
                globalRow ? '' : columns[hasHeader ? scope : 2] ?? '',
                this.partyOptional
            );
            if (row && validFlag && !invalid.has(index) && (hasHeader || columns.length <= (this.isPayout ? 4 : 3))) {
                rows.push(row);
            } else {
                skipped++;
            }
        });
        const issue = skipped
            ? `${label}: ${skipped} invalid row${skipped === 1 ? '' : 's'} skipped.`
            : rows.length
            ? ''
            : `${label}: no reference rows found.`;
        return { rows, issue };
    };

    get isPayout(): boolean {
        return this.scope === 'wallet';
    }

    get scopeLabel(): string {
        return this.isPayout ? 'Wallet ID' : 'Shop ID';
    }

    get targetLabel(): string {
        return TARGET_LABELS[this.target].id;
    }

    get columns(): string {
        return `${this.target}_id,party_id,${this.scope}_id`;
    }

    get sampleRow(): string {
        return `example-${this.target},example-party,example-${this.scope}`;
    }
}
