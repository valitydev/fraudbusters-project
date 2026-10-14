import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { debounceTime, map, take } from 'rxjs/operators';

import { removeEmptyProperties } from '../../../../utils/remove-empty-properties';
import { Filter } from '../../model/filter';

@Component({
    selector: 'fb-wb-list-search',
    templateUrl: 'wb-list-search.component.html',
})
export class WbListSearchComponent implements OnInit {
    @Output() valueChanges: EventEmitter<Filter> = new EventEmitter();

    @Input() listNames$: Observable<string[]>;

    form: FormGroup = this.fb.group({
        searchQuery: '',
        types: [],
    });

    constructor(private route: ActivatedRoute, private router: Router, private fb: FormBuilder) {
        this.form.valueChanges.pipe(debounceTime(600), map(removeEmptyProperties)).subscribe((v) => {
            this.router.navigate([location.pathname], { queryParams: v });
            this.valueChanges.emit({ searchValue: v.searchQuery, typeChanges: v.types });
        });
        this.route.queryParams.pipe(take(1)).subscribe((v) => this.form.patchValue(v));
    }

    ngOnInit(): void {
        this.listNames$.pipe(take(1)).subscribe((value) =>
            this.form.setValue({
                searchQuery: '',
                types: value,
            })
        );
    }
}
