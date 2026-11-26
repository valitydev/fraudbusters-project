import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

import { Notification } from '../../../../api/fb-management/swagger-codegen/model/notification';
import { NotificationService } from '../../services/notification.service';

interface LoadState {
    loading: boolean;
    failed: boolean;
    notification?: Notification;
}

@Component({
    templateUrl: './edit-notification.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false,
})
export class EditNotificationComponent {
    readonly reload$ = new BehaviorSubject<void>(undefined);

    state$ = combineLatest([this.route.params.pipe(map((params) => params.id as string)), this.reload$]).pipe(
        switchMap(([id]) =>
            this.notificationService.getNotificationById(id).pipe(
                map((notification): LoadState => ({ loading: false, failed: false, notification })),
                catchError(() => of<LoadState>({ loading: false, failed: true })),
                startWith<LoadState>({ loading: true, failed: false })
            )
        )
    );

    constructor(private route: ActivatedRoute, private notificationService: NotificationService) {}

    reload() {
        this.reload$.next();
    }
}
