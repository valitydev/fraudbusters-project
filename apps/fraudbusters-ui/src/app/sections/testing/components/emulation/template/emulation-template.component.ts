import { ChangeDetectionStrategy, Component } from '@angular/core';

import { PaymentEmulateFilter } from './model/payment-emulate-filter';
import { EmulationTemplateService } from '../../../services/emulation/emulation-template.service';

@Component({
    templateUrl: './emulation-template.component.html',
    styleUrls: ['./emulation-template.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmulationTemplateComponent {
    readonly view$ = this.emulationTemplateService.view$;
    readonly filter: PaymentEmulateFilter = { partyId: '', shopId: '' };

    constructor(private emulationTemplateService: EmulationTemplateService) {}

    emulate(): void {
        this.emulationTemplateService.emulateNext({ ...this.filter });
    }
}
