import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { debounceTime, map, take } from 'rxjs/operators';

import { removeEmptyProperties } from '../../../../../../shared/utils/remove-empty-properties';
import { LAYOUT_GAP_M } from '../../../../../../tokens';
import { BaseAnalyticsService } from '../../services/base-analytics.service';

@Component({
    selector: 'fb-base-analytics-search',
    templateUrl: 'base-analytics-search.component.html',
    styleUrls: ['search.component.scss'],
})
export class BaseAnalyticsSearchComponent implements OnInit {
    @Output() valueChanges: EventEmitter<string> = new EventEmitter();

    currencies$ = this.baseAnalyticsService.currencies$;

    @Input() inProgress: Observable<boolean>;

    units = ['1 day', '1 week', 'last month', 'last year'];

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
        private baseAnalyticsService: BaseAnalyticsService,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
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
}
