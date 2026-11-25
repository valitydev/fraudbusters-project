import { NgFor, NgIf, NgTemplateOutlet, SlicePipe } from '@angular/common';
import {
    ChangeDetectorRef,
    Component,
    ContentChild,
    EventEmitter,
    Input,
    NgZone,
    Output,
    TemplateRef,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export const CSV_MAX_FILE_SIZE = 2 * 1024 * 1024;

/** Result of parsing one CSV source: valid rows and a message about what was skipped, if anything. */
export interface CsvParseResult<T> {
    rows: T[];
    issue: string;
}

export type CsvParser<T> = (text: string, label: string) => CsvParseResult<T>;

/**
 * Bulk import panel shared by the reference and list forms: CSV files or pasted rows, a preview
 * and "Add N to queue". Parsing is up to the form; each preview row is rendered by the template
 * the form projects (`<ng-template let-row>`).
 */
@Component({
    selector: 'fb-csv-panel',
    standalone: true,
    imports: [NgFor, NgIf, NgTemplateOutlet, SlicePipe, MatButtonModule, MatIconModule],
    templateUrl: 'csv-panel.component.html',
    styleUrls: ['csv-panel.component.scss'],
})
export class CsvPanelComponent<T> {
    @Input() heading = 'Bulk import from CSV';
    @Input({ required: true }) description: string;
    @Input({ required: true }) columns: string;
    @Input({ required: true }) parse: CsvParser<T>;
    @Input() sampleRow = '';
    @Input() sampleName = 'sample.csv';
    /** Shown for a source with no valid rows, e.g. "no reference rows found". */
    @Input() emptyIssue = 'no rows found';

    @Output() readonly append = new EventEmitter<T[]>();
    @Output() readonly closed = new EventEmitter<void>();

    @ContentChild(TemplateRef) rowTemplate?: TemplateRef<{ $implicit: T }>;

    rows: T[] = [];
    reading = false;

    private fileRows: T[] = [];
    private pastedRows: T[] = [];
    private fileIssues: string[] = [];
    private pasteIssue = '';

    constructor(private zone: NgZone, private changeDetector: ChangeDetectorRef) {}

    get sampleUrl(): string {
        const sample = this.sampleRow ? `${this.columns}\n${this.sampleRow}\n` : `${this.columns}\n`;
        return 'data:text/csv;charset=utf-8,' + encodeURIComponent(sample);
    }

    get issues(): string[] {
        return this.pasteIssue ? [...this.fileIssues, this.pasteIssue] : this.fileIssues;
    }

    async loadFiles(input: HTMLInputElement): Promise<void> {
        const files = Array.from(input.files ?? []);
        input.value = '';
        if (!files.length || this.reading) {
            return;
        }
        this.reading = true;
        const results = await Promise.all(
            files.map(async (file): Promise<CsvParseResult<T>> => {
                if (!/\.csv$/i.test(file.name) || file.size > CSV_MAX_FILE_SIZE) {
                    return { rows: [], issue: `${file.name}: choose a CSV file up to 2 MB.` };
                }
                try {
                    const text = await file.text();
                    return text.trim()
                        ? this.parseSafely(text, file.name)
                        : { rows: [], issue: `${file.name}: ${this.emptyIssue}.` };
                } catch {
                    return { rows: [], issue: `${file.name}: could not read the file.` };
                }
            })
        );

        // Native file promises can complete outside Angular's change detection.
        this.zone.run(() => {
            this.fileRows = this.fileRows.concat(results.flatMap(({ rows }) => rows));
            this.rows = this.fileRows.concat(this.pastedRows);
            this.fileIssues = results.map(({ issue }) => issue).filter(Boolean);
            this.reading = false;
            this.changeDetector.markForCheck();
        });
    }

    parsePasted(text: string): void {
        const { rows, issue } = this.parseSafely(text, 'Pasted CSV');
        this.pastedRows = rows;
        this.pasteIssue = issue;
        this.rows = this.fileRows.concat(this.pastedRows);
    }

    reset(textarea: HTMLTextAreaElement): void {
        textarea.value = '';
        this.fileRows = [];
        this.pastedRows = [];
        this.fileIssues = [];
        this.pasteIssue = '';
        this.rows = [];
    }

    private parseSafely(text: string, label: string): CsvParseResult<T> {
        if (!text.trim()) {
            return { rows: [], issue: '' };
        }
        if (text.length > CSV_MAX_FILE_SIZE) {
            return { rows: [], issue: `${label}: CSV is too large.` };
        }
        const result = this.parse(text, label);
        return !result.rows.length && !result.issue ? { rows: [], issue: `${label}: ${this.emptyIssue}.` } : result;
    }
}
