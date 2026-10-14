import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, Inject, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';

import { GroupService } from './services/group.service';
import { PaymentTemplatesService } from '../../../api';
import { Group } from '../../../api/fb-management/swagger-codegen/model/group';
import { LAYOUT_GAP_L, LAYOUT_GAP_M } from '../../../tokens';
import { ErrorHandlerService } from '../../services/utils/error-handler.service';

@Component({
    selector: 'fb-create-group',
    templateUrl: './create-group.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [GroupService],
})
export class CreateGroupComponent implements OnInit {
    @Input() group: Group;

    options: string[] = [];

    forms = this.groupService.forms;
    form = this.groupService.form;

    saved$ = this.groupService.saved$;
    inProgress$ = this.groupService.inProgress$;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private groupService: GroupService,
        private templatesService: PaymentTemplatesService,
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar,
        @Inject(LAYOUT_GAP_L) public layoutGapL: string,
        @Inject(LAYOUT_GAP_M) public layoutGapM: string
    ) {}

    ngOnInit() {
        if (this.group) {
            this.form.setValue({ id: this.group.groupId });
            this.groupService.clear();
            this.groupService.addItems(this.group.priorityTemplates);
            this.form.get('id').disable();
        }
        this.saved$.subscribe(
            (groupId) => {
                if (this.group) {
                    this.snackBar.open(`Saved success: ${groupId}`, 'OK', {
                        duration: 3000,
                    });
                } else {
                    this.snackBar.open(`Group has been created`, 'OK', {
                        duration: 3000,
                    });
                    this.navigateToEdit(groupId);
                }
            },
            (error: HttpErrorResponse) => this.errorHandlerService.handleError(error, this.snackBar)
        );
        this.route.fragment.subscribe(() => {
            this.templatesService.getTemplatesName('').subscribe(
                (names) => {
                    this.options = names;
                },
                (error: HttpErrorResponse) => this.errorHandlerService.handleError(error, this.snackBar)
            );
        });
    }

    save() {
        this.groupService.save();
    }

    addItem() {
        this.groupService.addItem();
    }

    removeItem(index: number) {
        this.groupService.removeItem(index);
    }

    navigateToEdit(id) {
        this.router.navigate([`../groups/${id}`]);
    }

    back() {
        this.router.navigate([`../groups`]);
    }
}
