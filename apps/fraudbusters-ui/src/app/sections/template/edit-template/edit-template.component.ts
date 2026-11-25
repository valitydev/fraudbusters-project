import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, shareReplay, switchMap } from 'rxjs/operators';

import { TemplatesService } from '../services/templates/templates.service';

@Component({
    templateUrl: './edit-template.component.html',
    styleUrls: ['./edit-template.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditTemplateComponent {
    operationType$ = this.route.fragment;
    template$ = this.route.params.pipe(
        map((params) => params.id),
        switchMap((id) => {
            return this.templateService.getTemplates(1, id);
        }),
        map((response) => response.result),
        map((res) => res[0]),
        shareReplay(1)
    );

    constructor(private route: ActivatedRoute, private router: Router, private templateService: TemplatesService) {}

    back() {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        if (returnUrl) {
            void this.router.navigateByUrl(returnUrl);
        } else {
            void this.router.navigate([`../templates`]);
        }
    }
}
