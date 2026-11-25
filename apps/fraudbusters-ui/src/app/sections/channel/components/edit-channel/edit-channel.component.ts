import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';

import { Channel } from '../../../../api/fb-management/swagger-codegen/model/channel';
import { ChannelService } from '../../services/channel.service';

interface LoadState {
    loading: boolean;
    failed: boolean;
    channel?: Channel;
}

@Component({
    templateUrl: './edit-channel.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditChannelComponent {
    readonly reload$ = new BehaviorSubject<void>(undefined);

    state$ = combineLatest([this.route.params.pipe(map((params) => params.id as string)), this.reload$]).pipe(
        switchMap(([id]) =>
            this.channelService.getChannelById(id).pipe(
                map((channel): LoadState => ({ loading: false, failed: false, channel })),
                catchError(() => of<LoadState>({ loading: false, failed: true })),
                startWith<LoadState>({ loading: true, failed: false })
            )
        )
    );

    constructor(private route: ActivatedRoute, private channelService: ChannelService) {}

    reload() {
        this.reload$.next();
    }
}
