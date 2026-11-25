import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, shareReplay, switchMap } from 'rxjs/operators';

import { TemplatesService } from '../services/templates/templates.service';

@Component({
    templateUrl: './copy-template.component.html',
    styleUrls: ['./copy-template.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CopyTemplateComponent {
    template$ = this.route.params.pipe(
        map((params) => params.id),
        switchMap((id) => {
            return this.templateService.getTemplates(1, id);
        }),
        map((response) => response.result),
        map((res) => {
            res[0].id = res[0].id + '_COPY';
            return res[0];
        }),
        shareReplay(1)
    );

    constructor(private router: Router, private route: ActivatedRoute, private templateService: TemplatesService) {}

    back() {
        this.router.navigate([`../templates`]);
    }
}
