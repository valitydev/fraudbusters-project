import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, map, take } from 'rxjs/operators';

import { SearchFieldService } from '../../../../shared/services/utils/search-field.service';
import { removeEmptyProperties } from '../../../../shared/utils/remove-empty-properties';

@Component({
    selector: 'fb-history-data-search',
    templateUrl: 'history-data-search.component.html',
    styleUrls: ['search.component.scss'],
})
export class HistoryDataSearchComponent {
    @Input() showRuleFilters = false;
    @Output() valueChanges: EventEmitter<string> = new EventEmitter();

    form: FormGroup = this.fb.group({
        partyId: '',
        shopId: '',
        cardToken: '',
        country: '',
        status: '',
        paymentId: '',
        fingerprint: '',
        email: '',
        terminal: '',
        template: '',
        rule: '',
        from: this.searchFieldService.todayFromTime().toISOString(),
        to: new Date().toISOString(),
    });

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private searchFieldService: SearchFieldService
    ) {
        this.form.valueChanges.pipe(debounceTime(600), map(removeEmptyProperties)).subscribe((v) => {
            const params = Object.create(v);
            params.from = new Date(v.from).toISOString();
            params.to = new Date(v.to).toISOString();
            params.cardToken = v.cardToken;
            params.shopId = v.shopId;
            params.partyId = v.partyId;
            params.status = v.status;
            params.id = v.id;
            params.fingerprint = v.fingerprint;
            params.email = v.email;
            params.terminal = v.terminal;
            params.template = v.template;
            params.rule = v.rule;
            params.country = v.country;
            this.router.navigate([location.pathname], { queryParams: params });
            this.valueChanges.emit(v);
        });
        this.route.queryParams.pipe(take(1)).subscribe((v) => this.form.patchValue(v));
    }
}
