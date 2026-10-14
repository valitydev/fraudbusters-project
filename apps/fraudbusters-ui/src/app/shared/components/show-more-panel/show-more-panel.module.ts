import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ShowMorePanelComponent } from './show-more-panel.component';

@NgModule({
    declarations: [ShowMorePanelComponent],
    imports: [CommonModule, MatProgressSpinnerModule],
    exports: [ShowMorePanelComponent],
})
export class ShowMorePanelModule {}
