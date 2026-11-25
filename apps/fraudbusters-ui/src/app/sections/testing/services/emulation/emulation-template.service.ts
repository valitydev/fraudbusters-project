import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of, Subject } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

import { Template } from '../../../../api/fb-management/swagger-codegen/model/template';
import { PaymentEmulateService } from '../../../../api/payments/emulate';
import { ErrorHandlerService } from '../../../../shared/services/utils/error-handler.service';
import { PaymentEmulateFilter } from '../../components/emulation/template/model/payment-emulate-filter';

interface EmulationView {
    templates?: Template[];
    busy: boolean;
    failed: boolean;
}

@Injectable()
export class EmulationTemplateService {
    readonly view$: Observable<EmulationView>;

    private readonly queries = new Subject<PaymentEmulateFilter>();

    constructor(
        private paymentEmulateService: PaymentEmulateService,
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar
    ) {
        this.view$ = this.queries.pipe(
            switchMap((filter) =>
                this.paymentEmulateService.emulate(filter).pipe(
                    map((templates): EmulationView => ({ templates: templates ?? [], busy: false, failed: false })),
                    catchError((error: HttpErrorResponse) => {
                        this.errorHandlerService.handleError(error, this.snackBar);
                        return of({ busy: false, failed: true });
                    }),
                    startWith({ busy: true, failed: false })
                )
            ),
            startWith({ busy: false, failed: false })
        );
    }

    emulateNext(filter: PaymentEmulateFilter): void {
        this.queries.next(filter);
    }
}
