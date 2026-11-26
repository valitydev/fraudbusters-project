import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
    selector: 'fb-catalog-metadata-content',
    imports: [MatProgressBarModule],
    template: `
        @if (!ready) {
        <mat-progress-bar mode="indeterminate" aria-label="Loading catalog metadata"></mat-progress-bar>
        }
        <div [hidden]="!ready"><ng-content></ng-content></div>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogMetadataContentComponent {
    @Input() ready = false;
}
