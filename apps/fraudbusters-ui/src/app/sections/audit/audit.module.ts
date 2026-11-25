import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AuditRoutingModule } from './audit-routing.module';
import { AuditComponent } from './audit.component';
import { AuditService } from './audit.service';
import { AuditRemoteModule } from '../../api/audit';
import { DateTimeRangeComponent } from '../../shared/components/datetime-range/datetime-range.component';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';
import { SearchFieldService } from '../../shared/services/utils/search-field.service';

@NgModule({
    declarations: [AuditComponent],
    imports: [
        CommonModule,
        AuditRoutingModule,
        AuditRemoteModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        MatSelectModule,
        MatSnackBarModule,
        DateTimeRangeComponent,
    ],
    providers: [ErrorHandlerService, AuditService, DatePipe, SearchFieldService],
})
export class AuditModule {}
