import { HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Papa } from 'ngx-papaparse';
import { filter } from 'rxjs/operators';

import { FRAUD_REPORT_COLUMNS, fraudReportRow, FraudReportRow, missingColumns } from './fraud-report';
import { PaymentLoadDataService } from '../../../api/payments/load-data';

const MAX_FILE_SIZE = 10 * 1024 * 1024;
const PREVIEW_ROWS = 50;

/** Uploads a CSV of confirmed fraud payments after checking every row the way the backend will. */
@Component({
    templateUrl: './fraud-uploader.component.html',
    styleUrls: ['./fraud-uploader.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class FraudUploaderComponent {
    readonly columns = FRAUD_REPORT_COLUMNS.join(', ');
    readonly previewLimit = PREVIEW_ROWS;
    readonly sampleUrl =
        'data:text/csv;charset=utf-8,' +
        encodeURIComponent(
            `${FRAUD_REPORT_COLUMNS.join(',')}\n` +
                '100200.1,card_testing,Confirmed by the bank,2026-01-15 10:30:00\n' +
                '100201.1,account_takeover,,2026-01-15\n'
        );

    file: File | null = null;
    rows: FraudReportRow[] = [];
    fileIssue = '';
    dragging = false;
    uploading = false;
    error = '';

    constructor(
        private router: Router,
        private loadData: PaymentLoadDataService,
        private papa: Papa,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef,
        private destroyRef: DestroyRef
    ) {}

    get invalidRows(): FraudReportRow[] {
        return this.rows.filter((row) => row.issue);
    }

    get canUpload(): boolean {
        return Boolean(this.file && this.rows.length && !this.fileIssue && !this.invalidRows.length && !this.uploading);
    }

    get footerNote(): string {
        if (!this.file) {
            return 'Choose a CSV file to check it before uploading.';
        }
        if (this.fileIssue) {
            return this.fileIssue;
        }
        if (this.invalidRows.length) {
            const count = this.invalidRows.length;
            return `Fix ${count} ${
                count === 1 ? 'row' : 'rows'
            } and choose the file again: the backend rejects the whole file.`;
        }
        return `${this.rows.length} ${this.rows.length === 1 ? 'payment' : 'payments'} will be marked as fraud.`;
    }

    choose(input: HTMLInputElement): void {
        const file = input.files?.[0];
        input.value = '';
        if (file) {
            void this.read(file);
        }
    }

    drop(event: DragEvent): void {
        event.preventDefault();
        this.dragging = false;
        const file = event.dataTransfer?.files?.[0];
        if (file) {
            void this.read(file);
        }
    }

    dragOver(event: DragEvent): void {
        event.preventDefault();
        this.dragging = true;
    }

    removeFile(): void {
        this.file = null;
        this.rows = [];
        this.fileIssue = '';
        this.error = '';
    }

    back(): void {
        if (!this.uploading) {
            void this.router.navigate(['/historical-data/payments/fraud-payments']);
        }
    }

    upload(): void {
        if (!this.canUpload) {
            return;
        }
        this.uploading = true;
        this.error = '';
        const count = this.rows.length;
        this.loadData
            .postFile(this.file)
            .pipe(
                filter((event) => event.type === HttpEventType.Response),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: () => {
                    this.uploading = false;
                    this.snackBar.open(`${count} fraud ${count === 1 ? 'payment' : 'payments'} loaded`, 'OK', {
                        duration: 3000,
                    });
                    this.back();
                },
                error: (error: HttpErrorResponse) => {
                    this.uploading = false;
                    this.error =
                        error.status === 400 || error.status === 422
                            ? 'The server rejected the file. Check the columns and dates.'
                            : 'Could not upload the file. Try again.';
                    this.cdr.markForCheck();
                },
            });
    }

    private async read(file: File): Promise<void> {
        this.file = file;
        this.rows = [];
        this.error = '';
        this.fileIssue = '';
        if (!/\.csv$/i.test(file.name) || file.size > MAX_FILE_SIZE) {
            this.fileIssue = `${file.name}: choose a CSV file up to 10 MB.`;
        } else {
            try {
                this.parse(await file.text());
            } catch {
                this.fileIssue = `${file.name}: could not read the file.`;
            }
        }
        this.cdr.markForCheck();
    }

    private parse(text: string): void {
        const parsed = this.papa.parse(text.trim(), {
            header: true,
            skipEmptyLines: 'greedy',
            transformHeader: (name: string) => name.trim().toLowerCase(),
        });
        const missing = missingColumns(parsed.meta?.fields ?? []);
        if (missing.length) {
            this.fileIssue = `The file has no ${missing.join(', ')} column${missing.length === 1 ? '' : 's'}.`;
            return;
        }
        this.rows = (parsed.data as Record<string, string>[]).map((record, index) => fraudReportRow(record, index + 2));
        if (!this.rows.length) {
            this.fileIssue = 'The file has no rows.';
        }
    }
}
