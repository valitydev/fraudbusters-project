import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';

import { CatalogSearchComponent } from '../catalog-search/catalog-search.component';

@Component({
    selector: 'fb-catalog-panel',
    imports: [NgTemplateOutlet, MatButtonModule, MatProgressBarModule, MatSelectModule, CatalogSearchComponent],
    templateUrl: 'catalog-panel.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPanelComponent {
    @Input() label: string;
    @Input() searchLabel: string;
    @Input() searchParam = 'searchQuery';
    @Input() itemName: string;
    @Input() singularItemName?: string;
    @Input() count = 0;
    @Input() total?: number;
    @Input() visible = 0;
    @Input() paginated = false;
    @Input() continuationAvailable?: boolean;
    @Input() summaryInFooter = false;
    @Input() busy = false;
    @Input() failed = false;
    @Input() sort: string;
    @Input() sortOptions: readonly { value: string; label: string }[] = [];

    @Output() readonly sortChange = new EventEmitter<string>();
    @Output() readonly more = new EventEmitter<void>();
    @Output() readonly retry = new EventEmitter<void>();
}
