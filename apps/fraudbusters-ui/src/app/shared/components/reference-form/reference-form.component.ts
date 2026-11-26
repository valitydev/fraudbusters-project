import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, ViewChild, ElementRef, Input, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { defer, from, of } from 'rxjs';
import {
    catchError,
    concatMap,
    debounceTime,
    distinctUntilChanged,
    endWith,
    finalize,
    ignoreElements,
    map,
    startWith,
    switchMap,
    tap,
} from 'rxjs/operators';

import { ReferenceCsvComponent } from './reference-csv.component';
import {
    normalizeReference,
    referenceKey,
    ReferenceDraft,
    ReferenceFormConfig,
    TARGET_LABELS,
} from './reference-draft';

@Component({
    selector: 'fb-reference-form',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        ReferenceCsvComponent,
    ],
    templateUrl: 'reference-form.component.html',
    styleUrls: ['../form-page/form-page.scss'],
})
export class ReferenceFormComponent {
    @Input({ required: true }) config: ReferenceFormConfig;

    readonly form = inject(FormBuilder).nonNullable.group({
        targetId: [''],
        partyId: [''],
        scopeId: [''],
    });

    readonly targetNames$ = this.form.controls.targetId.valueChanges.pipe(
        startWith(''),
        map((id) => id.trim()),
        debounceTime(200),
        distinctUntilChanged(),
        switchMap((id) => this.config.loadTargetNames(id).pipe(catchError(() => of([]))))
    );

    staged: ReferenceDraft[] = [];
    csvOpen = false;
    saving = false;
    notice = '';
    error = '';

    @ViewChild('targetInput') private targetInput: ElementRef<HTMLInputElement>;
    private readonly stagedKeys = new Set<string>();

    constructor(
        private readonly destroyRef: DestroyRef,
        private readonly router: Router,
        private readonly route: ActivatedRoute
    ) {}

    get isPayout(): boolean {
        return this.config.scope === 'wallet';
    }

    get target() {
        return TARGET_LABELS[this.config.target ?? 'template'];
    }

    get partyOptional(): boolean {
        return Boolean(this.config.partyOptional);
    }

    /** What still has to be filled in before the current row can be added. */
    get requiredFields(): string {
        if (!this.partyOptional) {
            return `${this.target.id} and Party ID`;
        }
        const { partyId, scopeId } = this.form.getRawValue();
        return scopeId.trim() && !partyId.trim() ? `Party ID for the ${this.config.scope}` : this.target.id;
    }

    get scopeLabel(): string {
        return this.isPayout ? 'Wallet' : 'Shop';
    }

    get scopePlural(): string {
        return this.isPayout ? 'wallets' : 'shops';
    }

    get draft(): ReferenceDraft | null {
        const { targetId, partyId, scopeId } = this.form.getRawValue();
        return normalizeReference(targetId, partyId, scopeId, this.partyOptional);
    }

    get draftIncomplete(): boolean {
        const { targetId, partyId, scopeId } = this.form.getRawValue();
        return !this.draft && Boolean(targetId.trim() || partyId.trim() || scopeId.trim());
    }

    get createCount(): number {
        const draft = this.draft;
        return this.staged.length + Number(Boolean(draft && !this.isStaged(draft)));
    }

    back(): void {
        if (!this.saving) {
            void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || this.config.backUrl);
        }
    }

    addReference(): void {
        const draft = this.draft;
        if (!draft || this.saving) {
            return;
        }
        if (this.isStaged(draft)) {
            this.notice = 'This reference is already queued.';
            return;
        }
        this.staged.push(draft);
        this.stagedKeys.add(referenceKey(draft));
        this.form.reset();
        this.notice = '';
        this.targetInput.nativeElement.focus();
    }

    addCsvRows(rows: ReferenceDraft[]): void {
        let added = 0;
        for (const row of rows) {
            const key = referenceKey(row);
            if (!this.stagedKeys.has(key)) {
                this.staged.push(row);
                this.stagedKeys.add(key);
                added++;
            }
        }
        const skipped = rows.length - added;
        this.notice = `Added ${added} from CSV.${
            skipped ? ` Skipped ${skipped} duplicate row${skipped === 1 ? '' : 's'}.` : ''
        }`;
        this.csvOpen = false;
    }

    removeRow(index: number): void {
        const [removed] = this.staged.splice(index, 1);
        this.stagedKeys.delete(referenceKey(removed));
        this.notice = '';
    }

    clearQueue(): void {
        this.staged = [];
        this.stagedKeys.clear();
        this.notice = '';
    }

    create(): void {
        if (!this.createCount || this.draftIncomplete || this.saving) {
            return;
        }
        const draft = this.draft;
        const rows = draft && !this.isStaged(draft) ? [...this.staged, draft] : [...this.staged];
        this.error = '';
        this.saving = true;
        this.form.disable({ emitEvent: false });
        const config = this.config;
        let saved = 0;

        defer(() =>
            'saveBatch' in config
                ? config.saveBatch(rows)
                : from(rows).pipe(
                      concatMap((row) =>
                          config.saveRow(row).pipe(
                              tap(() => {
                                  saved++;
                                  this.removeSavedRow(row);
                              })
                          )
                      )
                  )
        )
            .pipe(
                ignoreElements(),
                endWith(undefined),
                takeUntilDestroyed(this.destroyRef),
                finalize(() => {
                    this.saving = false;
                    this.form.enable({ emitEvent: false });
                })
            )
            .subscribe({
                next: () => {
                    this.saving = false;
                    config.onSaved?.();
                    this.back();
                },
                error: ({ status }: HttpErrorResponse) => {
                    this.error = saved
                        ? `${saved} of ${rows.length} created. The remaining references are ready to retry.`
                        : status === 422
                        ? `The server rejected these references. Check the ${this.target.noun} IDs and scopes.`
                        : 'Could not create the references. Review the current references before retrying.';
                },
            });
    }

    private removeSavedRow(row: ReferenceDraft): void {
        const key = referenceKey(row);
        const index = this.staged.findIndex((item) => referenceKey(item) === key);
        if (index !== -1) {
            this.removeRow(index);
        }
        const draft = this.draft;
        if (draft && referenceKey(draft) === key) {
            this.form.reset(undefined, { emitEvent: false });
        }
    }

    private isStaged(row: ReferenceDraft): boolean {
        return this.stagedKeys.has(referenceKey(row));
    }
}
