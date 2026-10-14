import { Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { OperationType } from '../../../../../shared/constants/operation-type';
import { CsvUtilsService } from '../../../../../shared/services/utils/csv-utils.service';
import { LAYOUT_GAP_M } from '../../../../../tokens';
import { CreatePaymentReferenceService } from '../../services/create-payment-reference.service';

@Component({
    templateUrl: 'create-payment-reference.component.html',
    providers: [CreatePaymentReferenceService, CsvUtilsService],
})
export class CreatePaymentReferenceComponent {
    forms = this.createPaymentReferenceService.forms;
    operationType = OperationType;
    inProgress$ = this.createPaymentReferenceService.inProgress$;

    constructor(
        private createPaymentReferenceService: CreatePaymentReferenceService,
        private route: ActivatedRoute,
        private router: Router,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.createPaymentReferenceService.created$.subscribe(() => {
            void this.router.navigateByUrl(
                this.route.snapshot.queryParamMap.get('returnUrl') || '/templates/references'
            );
        });
    }

    addItem() {
        this.createPaymentReferenceService.addItem();
    }

    removeItem(index: number) {
        this.createPaymentReferenceService.removeItem(index);
    }

    createReference() {
        this.createPaymentReferenceService.create();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    prepareFilesList(files: Array<any>): void {
        this.createPaymentReferenceService.prepareFilesList(files);
    }
}
