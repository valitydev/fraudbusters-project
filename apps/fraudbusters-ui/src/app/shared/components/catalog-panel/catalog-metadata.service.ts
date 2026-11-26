import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { animationFrameScheduler, merge, Observable, of, Subject } from 'rxjs';
import {
    catchError,
    delay,
    distinctUntilChanged,
    map,
    scan,
    shareReplay,
    startWith,
    switchMap,
    take,
} from 'rxjs/operators';

import { GroupsMetadata } from '../../../api/fb-management/swagger-codegen/model/groupsMetadata';
import { PaymentListsMetadata } from '../../../api/fb-management/swagger-codegen/model/paymentListsMetadata';
import { PaymentTemplatesMetadata } from '../../../api/fb-management/swagger-codegen/model/paymentTemplatesMetadata';
import { PayoutListsMetadata } from '../../../api/fb-management/swagger-codegen/model/payoutListsMetadata';
import { PayoutTemplatesMetadata } from '../../../api/fb-management/swagger-codegen/model/payoutTemplatesMetadata';
import { ConfigService } from '../../../config';

export type CatalogSection = 'templates' | 'groups' | 'lists';

type CatalogMetadataResponse =
    | GroupsMetadata
    | PaymentListsMetadata
    | PaymentTemplatesMetadata
    | PayoutListsMetadata
    | PayoutTemplatesMetadata;

interface CatalogMetadataState {
    counts?: Record<string, number> | null;
    listNames?: Record<string, string[]>;
    showData: boolean;
}

interface CatalogMetadataView extends CatalogMetadataState {
    count(tab: string): number | '—';
}

const NO_LIST_NAMES: string[] = [];
const COUNT_KEYS: Record<string, string> = {
    list: 'groups',
    'default-references': 'defaultReferences',
    candidates: 'candidateBatches',
};

@Injectable()
export class CatalogMetadataService {
    private readonly reload$ = new Subject<void>();
    private view$: Observable<CatalogMetadataView>;

    constructor(private http: HttpClient, private config: ConfigService) {}

    load(domain: 'payments' | 'payouts', section: CatalogSection): Observable<CatalogMetadataView> {
        const url = `${this.config.fbManagementEndpoint}/${domain}/catalog/${section}/metadata`;
        const response$ = this.reload$.pipe(
            startWith(undefined),
            switchMap(() =>
                this.http.get<CatalogMetadataResponse>(url).pipe(
                    map((response) => ({
                        counts: { ...response.counts },
                        listNames: 'listNames' in response ? { ...response.listNames } : undefined,
                    })),
                    catchError(() => of({ counts: null, listNames: undefined }))
                )
            ),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        const initial: CatalogMetadataState = { showData: false };

        this.view$ = merge(
            response$,
            response$.pipe(
                take(1),
                // Give the badges a paint before revealing even an already loaded table.
                delay(0, animationFrameScheduler),
                delay(0, animationFrameScheduler),
                map(() => ({ showData: true }))
            )
        ).pipe(
            scan((view, update) => ({ ...view, ...update }), initial),
            startWith(initial),
            map((view) => ({
                ...view,
                count: (tab: string) =>
                    view.counts === null ? ('—' as const) : view.counts?.[COUNT_KEYS[tab] ?? tab] ?? 0,
            })),
            shareReplay({ bufferSize: 1, refCount: true })
        );
        return this.view$;
    }

    listNames(listType: string): Observable<string[]> {
        return this.view$.pipe(
            map((view) => view.listNames?.[listType === 'naming' ? 'named' : listType] ?? NO_LIST_NAMES),
            distinctUntilChanged()
        );
    }

    refresh(): void {
        this.reload$.next();
    }
}
