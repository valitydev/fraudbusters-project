import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { shareReplay, distinctUntilChanged } from 'rxjs/operators';

import { Channel } from '../../../api/fb-management/swagger-codegen/model/channel';
import { NotificationsService } from '../../../api/payments/notifications';
import { ConfigService } from '../../../config';
import { FetchResult, PartialFetcher } from '../../../shared/utils/partial-fetcher';

export interface FetchChannelsParams {
    searchValue?: string;
    lastId?: string;
    size?: number;
}

@Injectable()
export class FetchChannelsService extends PartialFetcher<Channel, FetchChannelsParams> {
    inProgress$ = this.doAction$.pipe(distinctUntilChanged(), shareReplay(1));
    private pageSize = this.configService.pageSize;

    constructor(private notificationsService: NotificationsService, private configService: ConfigService) {
        super();
    }

    protected fetch(params: FetchChannelsParams): Observable<FetchResult<Channel>> {
        const { searchValue, size, lastId } = params;
        return this.notificationsService.getChannels({
            searchValue: searchValue || '',
            size: size ? size : this.pageSize,
            ...(lastId ? { lastId } : {}),
        });
    }
}
