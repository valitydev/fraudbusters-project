import { NgIf } from '@angular/common';
import { Component, DestroyRef, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime } from 'rxjs/operators';

@Component({
    standalone: true,
    imports: [NgIf, MatIconModule, ReactiveFormsModule],
    selector: 'fb-catalog-search',
    templateUrl: 'catalog-search.component.html',
    styleUrls: ['catalog-search.component.scss'],
})
export class CatalogSearchComponent implements OnInit {
    @Input() label = 'Search templates by ID or rule';
    @Input() queryParam = 'searchQuery';
    readonly query = new FormControl('', { nonNullable: true });

    @ViewChild('searchInput') private input?: ElementRef<HTMLInputElement>;

    constructor(private route: ActivatedRoute, private router: Router, private readonly destroyRef: DestroyRef) {}

    ngOnInit(): void {
        this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
            const value = params.get(this.queryParam) ?? '';
            this.query.setValue(value, { emitEvent: false });
        });
        this.query.valueChanges.pipe(debounceTime(200), takeUntilDestroyed(this.destroyRef)).subscribe((value) => {
            void this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { [this.queryParam]: value.trim() ? value : null },
                queryParamsHandling: 'merge',
                replaceUrl: true,
            });
        });
    }

    @HostListener('document:keydown', ['$event'])
    focusFromKeyboard(event: KeyboardEvent): void {
        if (event.key !== '/' || event.altKey || event.ctrlKey || event.metaKey || event.defaultPrevented) {
            return;
        }
        const target = event.target as HTMLElement;
        if (
            target?.closest(
                'input, textarea, select, [contenteditable], [role="dialog"], [role="menu"], [role="combobox"], [role="listbox"]'
            )
        ) {
            return;
        }
        event.preventDefault();
        this.input?.nativeElement.focus();
    }
}
