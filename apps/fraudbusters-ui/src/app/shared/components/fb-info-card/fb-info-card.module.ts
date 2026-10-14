import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexModule } from '@angular/flex-layout';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { FbInfoCardComponent } from './fb-info-card.component';
import { SharedPipesModule } from '../../pipes';

@NgModule({
    declarations: [FbInfoCardComponent],
    exports: [FbInfoCardComponent],
    imports: [MatCardModule, FlexModule, CommonModule, MatProgressSpinnerModule, SharedPipesModule],
})
export class FbInfoCardModule {}
