import { CommonModule } from '@angular/common';
import {
    ChangeDetectorRef,
    Component,
    DestroyRef,
    ElementRef,
    EventEmitter,
    Input,
    OnInit,
    Output,
    ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, Observable, Subject, combineLatest, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap } from 'rxjs/operators';

const DEFAULT_WEIGHT = 1;

interface GroupTemplate {
    id: string;
    priority: number;
}

export interface GroupEditorConfig {
    backUrl: string;
    loadGroup: (id: string) => Observable<{ groupId?: string; priorityTemplates?: Partial<GroupTemplate>[] }>;
    loadTemplateNames: (prefix: string) => Observable<string[]>;
    save: (group: { groupId: string; priorityTemplates: GroupTemplate[] }) => Observable<string>;
}

@Component({
    selector: 'fb-group-editor',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatAutocompleteModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
    ],
    templateUrl: 'group-editor.component.html',
    styleUrls: ['group-editor.component.scss'],
})
export class GroupEditorComponent implements OnInit {
    @Input({ required: true }) config: GroupEditorConfig;
    @Output() readonly saved = new EventEmitter<{ id: string; editing: boolean }>();
    readonly details = this.builder.nonNullable.group({ groupId: '' });

    readonly entry = this.builder.group({
        templateId: this.builder.nonNullable.control(''),
        priority: this.builder.control<number | null>(DEFAULT_WEIGHT),
    });

    readonly templateNames$ = this.entry.controls.templateId.valueChanges.pipe(
        startWith(''),
        map((name) => name.trim()),
        distinctUntilChanged(),
        debounceTime(200),
        switchMap((name) => this.config.loadTemplateNames(name).pipe(catchError(() => of([]))))
    );

    staged: GroupTemplate[] = [];
    editing = false;
    loading = false;
    loadFailed = false;
    busy = false;
    error = '';
    notice = '';
    readonly minimumWeight = 0;

    @ViewChild('templateInput') private templateInput: ElementRef<HTMLInputElement>;
    private readonly reload = new Subject<void>();
    private originalPriorities = new Map<string, number>();

    constructor(
        private readonly router: Router,
        private readonly route: ActivatedRoute,
        private readonly destroyRef: DestroyRef,
        private readonly builder: FormBuilder,
        private readonly changeDetector: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        const id$ = this.route.paramMap.pipe(
            map((params) => params.get('id')),
            distinctUntilChanged()
        );
        const copyFrom$ = this.route.queryParamMap.pipe(
            map((params) => params.get('copyFrom')),
            distinctUntilChanged()
        );
        combineLatest([id$, copyFrom$, this.reload.pipe(startWith(undefined))])
            .pipe(
                switchMap(([id, copyFrom]) => {
                    const source = id || copyFrom;
                    this.editing = !!id;
                    this.loading = !!source;
                    this.loadFailed = false;
                    this.error = '';
                    this.notice = '';
                    this.changeDetector.markForCheck();
                    return source
                        ? this.config.loadGroup(source).pipe(
                              catchError(() => {
                                  this.loading = false;
                                  this.loadFailed = true;
                                  this.changeDetector.markForCheck();
                                  return EMPTY;
                              })
                          )
                        : of(null);
                }),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe((group) => {
                this.loading = false;
                this.details.reset({ groupId: this.editing ? group?.groupId ?? '' : '' });
                this.entry.reset({ templateId: '', priority: DEFAULT_WEIGHT });
                this.staged = (group?.priorityTemplates ?? [])
                    .map(({ id, priority }) => ({ id, priority }))
                    .sort((left, right) => left.priority - right.priority);
                this.originalPriorities = new Map(this.staged.map(({ id, priority }) => [id, priority]));
                this.changeDetector.markForCheck();
            });
    }

    retryLoad(): void {
        this.reload.next();
    }

    get groupId(): string {
        const id = this.details.controls.groupId.value;
        return this.editing ? id : id.trim();
    }

    get validName(): boolean {
        return this.editing ? !!this.groupId : /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(this.groupId);
    }

    get pending(): GroupTemplate | null {
        const { templateId, priority } = this.entry.getRawValue();
        const id = templateId.trim();
        return id && this.validPriority(priority) ? { id, priority } : null;
    }

    get invalidDraft(): boolean {
        return !!this.entry.controls.templateId.value.trim() && !this.pending;
    }

    get validQueue(): boolean {
        return this.staged.every((row) => this.validStagedPriority(row));
    }

    get hasChanges(): boolean {
        const { templateId, priority } = this.entry.getRawValue();
        return (
            !!templateId.trim() ||
            priority !== DEFAULT_WEIGHT ||
            this.staged.length !== this.originalPriorities.size ||
            this.staged.some((row) => this.templateChange(row) !== '')
        );
    }

    get orderedTemplates(): GroupTemplate[] {
        return [...this.staged].sort((left, right) => left.priority - right.priority);
    }

    get executionOrder(): ReadonlyMap<number, number> {
        const ranks = new Map<number, number>();
        this.orderedTemplates
            .filter((row) => this.validStagedPriority(row))
            .forEach(({ priority }, index) => {
                if (!ranks.has(priority)) {
                    ranks.set(priority, index + 1);
                }
            });
        return ranks;
    }

    templateChange({ id, priority }: GroupTemplate): '' | 'Added' | 'Weight changed' {
        const original = this.originalPriorities.get(id);
        if (original === undefined) {
            return 'Added';
        }
        return priority === original ? '' : 'Weight changed';
    }

    discardChanges(): void {
        this.staged = Array.from(this.originalPriorities, ([id, priority]) => ({ id, priority }));
        this.entry.reset({ templateId: '', priority: DEFAULT_WEIGHT });
        this.error = '';
        this.notice = '';
    }

    get canSave(): boolean {
        return this.validName && this.validQueue && !this.invalidDraft && (!!this.pending || this.staged.length > 0);
    }

    get templateCount(): number {
        const pending = this.pending;
        return this.staged.length + Number(!!pending && !this.staged.some(({ id }) => id === pending.id));
    }

    validPriority(priority: number | null): boolean {
        return Number.isSafeInteger(priority) && priority >= this.minimumWeight;
    }

    validStagedPriority({ id, priority }: GroupTemplate): boolean {
        return (
            this.validPriority(priority) ||
            (Number.isSafeInteger(priority) && this.originalPriorities.get(id) === priority)
        );
    }

    stage(): void {
        const row = this.pending;
        if (this.busy || !row) {
            return;
        }
        this.notice = this.staged.some(({ id }) => id === row.id) ? 'Updated the template weight.' : '';
        this.staged = this.withTemplate(row).sort((left, right) => left.priority - right.priority);
        this.entry.reset({ templateId: '', priority: DEFAULT_WEIGHT });
        this.templateInput.nativeElement.focus();
    }

    trackTemplate(_index: number, row: GroupTemplate): string {
        return row.id;
    }

    moveTemplate(row: GroupTemplate, step: -1 | 1): void {
        if (this.busy || !this.validQueue) {
            return;
        }
        const ordered = this.orderedTemplates;
        const index = ordered.indexOf(row);
        const target = index + step;
        if (index < 0 || target < 0 || target >= ordered.length) {
            return;
        }

        const weights = ordered.map(({ priority }) => priority);
        const first = weights.indexOf(weights[Math.min(index, target)]);
        const last = weights.lastIndexOf(weights[Math.max(index, target)]);
        [ordered[index], ordered[target]] = [ordered[target], ordered[index]];

        // Keep the existing weight slots where possible. Make any affected ties
        // explicit so the move survives saving and reloading the group.
        let previous = -1;
        for (let position = first; position <= last; position++) {
            weights[position] = Math.max(weights[position], previous + 1);
            previous = weights[position];
        }
        let original: number | undefined;
        for (let position = last + 1; position < weights.length; position++) {
            if (weights[position] !== original) {
                original = weights[position];
                previous = Math.max(original, previous + 1);
            }
            weights[position] = previous;
        }
        const fit = Number.isSafeInteger(weights[weights.length - 1]);
        this.staged = ordered.map((template, position) => ({
            id: template.id,
            priority: fit ? weights[position] : position,
        }));
        this.notice = '';
    }

    updateWeight(row: GroupTemplate, value: number): void {
        row.priority = value;
        this.notice = '';
    }

    remove(row: GroupTemplate): void {
        this.staged = this.staged.filter((item) => item !== row);
        this.notice = '';
    }

    clear(): void {
        this.staged = [];
        this.notice = '';
    }

    back(): void {
        if (!this.busy) {
            void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || this.config.backUrl);
        }
    }

    save(): void {
        if (this.busy || !this.canSave || this.loading || this.loadFailed) {
            return;
        }
        const pending = this.pending;
        const priorityTemplates = pending ? this.withTemplate(pending) : this.staged;
        const groupId = this.groupId;
        const editing = this.editing;
        this.busy = true;
        this.error = '';
        this.details.disable({ emitEvent: false });
        this.entry.disable({ emitEvent: false });

        this.config
            .save({ groupId, priorityTemplates })
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                finalize(() => {
                    this.busy = false;
                    this.details.enable({ emitEvent: false });
                    this.entry.enable({ emitEvent: false });
                    this.changeDetector.markForCheck();
                })
            )
            .subscribe({
                next: (savedId) => {
                    if (editing) {
                        this.staged = priorityTemplates;
                        this.originalPriorities = new Map(priorityTemplates.map(({ id, priority }) => [id, priority]));
                        this.entry.reset({ templateId: '', priority: DEFAULT_WEIGHT });
                        this.notice = '';
                    }
                    this.saved.emit({ id: savedId || groupId, editing });
                },
                error: ({ status }) => {
                    this.error =
                        status === 422 ? 'Check that all template IDs exist.' : 'Could not save the group. Try again.';
                },
            });
    }

    private withTemplate(row: GroupTemplate): GroupTemplate[] {
        return this.staged.some(({ id }) => id === row.id)
            ? this.staged.map((item) => (item.id === row.id ? row : item))
            : [...this.staged, row];
    }
}
