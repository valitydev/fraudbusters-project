import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ShowMoreContinuationPanelComponent } from './show-more-continuation-panel.component';

@NgModule({
    declarations: [ShowMoreContinuationPanelComponent],
    imports: [CommonModule, MatProgressSpinnerModule],
    exports: [ShowMoreContinuationPanelComponent],
})
export class ShowMoreContinuationPanelModule {}
