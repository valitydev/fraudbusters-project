import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { debounceTime, map, take } from 'rxjs/operators';

import { removeEmptyProperties } from '../../../../../../shared/utils/remove-empty-properties';
import { BaseAnalyticsService } from '../../services/base-analytics.service';

@Component({
    selector: 'fb-base-analytics-search',
    templateUrl: 'base-analytics-search.component.html',
})
export class BaseAnalyticsSearchComponent implements OnInit {
    @Output() valueChanges: EventEmitter<string> = new EventEmitter();

    currencies$ = this.baseAnalyticsService.currencies$;

    @Input() inProgress: Observable<boolean>;

    readonly units = ['1 day', '1 week', 'last month', 'last year'];

    form: FormGroup = this.fb.group({
        partyId: '',
        shopId: '',
        type: '',
        time: this.units[0],
    });

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private baseAnalyticsService: BaseAnalyticsService
    ) {
        this.form.valueChanges.pipe(debounceTime(600), map(removeEmptyProperties)).subscribe((v) => {
            this.router.navigate([location.pathname], { queryParams: v });
            const params = Object.create(v);
            params.partyId = v.partyId;
            params.shopId = v.shopId;
            params.type = v.type;
            params.time = v.time;
            this.valueChanges.emit(v);
        });
        this.route.queryParams.pipe(take(1)).subscribe((v) => this.form.patchValue(v));
    }

    ngOnInit(): void {
        this.currencies$.pipe(take(1)).subscribe((value) => {
            this.form.setValue({
                partyId: this.route.snapshot.queryParamMap.get('partyId')
                    ? this.route.snapshot.queryParamMap.get('partyId')
                    : '',
                shopId: this.route.snapshot.queryParamMap.get('shopId')
                    ? this.route.snapshot.queryParamMap.get('shopId')
                    : '',
                type: this.route.snapshot.queryParamMap.get('type')
                    ? this.route.snapshot.queryParamMap.get('type')
                    : value[0],
                time: this.route.snapshot.queryParamMap.get('time')
                    ? this.route.snapshot.queryParamMap.get('time')
                    : this.units[0],
            });
        });
    }

    setPeriod(unit: string): void {
        if (this.form.value.time !== unit) {
            this.form.patchValue({ time: unit });
        }
    }

    hasActiveFilters(defaultCurrency: string): boolean {
        const { partyId, shopId, type, time } = this.form.value;
        return Boolean(partyId || shopId || type !== defaultCurrency || time !== this.units[0]);
    }

    resetFilters(defaultCurrency: string): void {
        this.form.reset({ partyId: '', shopId: '', type: defaultCurrency, time: this.units[0] });
    }
}
