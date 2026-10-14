import { Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { LAYOUT_GAP_M } from '../../../../../tokens';
import { CreateDefaultPaymentReferenceService } from '../../services/create-default-payment-reference.service';

@Component({
    templateUrl: 'create-default-payment-reference.component.html',
    providers: [CreateDefaultPaymentReferenceService],
})
export class CreateDefaultPaymentReferenceComponent {
    form = this.createDefaultPaymentReferenceService.form;
    inProgress$ = this.createDefaultPaymentReferenceService.inProgress$;

    constructor(
        private createDefaultPaymentReferenceService: CreateDefaultPaymentReferenceService,
        private route: ActivatedRoute,
        private router: Router,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {
        this.createDefaultPaymentReferenceService.created$.subscribe(() => {
            void this.router.navigateByUrl(
                this.route.snapshot.queryParamMap.get('returnUrl') || '/templates/default-references'
            );
        });
    }

    createReference() {
        this.createDefaultPaymentReferenceService.create();
    }
}
