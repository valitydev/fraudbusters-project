import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, of, ReplaySubject, Subject } from 'rxjs';
import { catchError, debounceTime, map, scan, switchMap, tap } from 'rxjs/operators';

import { Filter } from './model/filter';
import { Log } from './model/log';
import { AuditRemoteService } from '../../api/audit';
import { DateFormat } from '../../shared/constants/date-format';
import { SortOrder } from '../../shared/constants/sort-order';
import { ErrorHandlerService } from '../../shared/services/utils/error-handler.service';

@Injectable()
export class AuditService {
    searchFilter$ = new ReplaySubject<Filter>();
    searchField$ = new Subject<string>();

    sort$ = new BehaviorSubject<SortOrder>(SortOrder.Desc);
    last: Log;
    loadMoreAction$ = new BehaviorSubject<boolean>(false);

    commandsTypes$: Observable<string[]>;
    objectsTypes$: Observable<string[]>;
    logs$: Observable<Log[]>;

    isLoadMore$ = new Observable<boolean>();
    isLoadMoreSubject$ = new BehaviorSubject<boolean>(false);

    count = 0;
    isRefresh;

    private readonly size = 10;

    constructor(
        private errorHandlerService: ErrorHandlerService,
        private snackBar: MatSnackBar,
        private auditRemoteService: AuditRemoteService,
        private router: Router,
        public datepipe: DatePipe
    ) {
        this.commandsTypes$ = auditRemoteService.getCommandTypes();
        this.objectsTypes$ = auditRemoteService.getObjectTypes();

        this.searchField$
            .pipe(
                debounceTime(500),
                tap((value) => {
                    this.mergeQueryParam({
                        userId: value,
                    });
                    this.isRefresh = true;
                })
            )
            .subscribe();

        this.sort$.subscribe(() => (this.isRefresh = true));
        this.loadMoreAction$.subscribe(() => (this.isRefresh = false));
        this.logs$ = combineLatest([this.searchFilter$, this.loadMoreAction$, this.sort$]).pipe(
            switchMap((value) => {
                return this.auditRemoteService
                    .findLogs({
                        searchValue: value[0].user + '%',
                        objectTypes: value[0].objectTypes,
                        commandTypes: value[0].commandTypes,
                        from: this.datepipe.transform(value[0].from, DateFormat._yyyyMMDdHHMmSs),
                        to: this.datepipe.transform(value[0].to, DateFormat._yyyyMMDdHHMmSs),
                        sortOrder: SortOrder.Desc,
                        size: this.size,
                        lastId: this.isLoadMore(value) ? this.last.id : null,
                        sortFieldValue: this.isLoadMore(value) ? this.last.insertTime : null,
                    })
                    .pipe(
                        catchError((error) => {
                            this.errorHandlerService.handleError(error, this.snackBar);
                            return of(error);
                        }),
                        map((ref) => {
                            this.last = ref.result ? ref.result[ref.result.length - 1] : [];
                            this.count = ref.count;
                            return { logs: ref.result, filter: value, count: ref.count };
                        })
                    );
            }),
            scan((logs, result) => {
                let logsRes;
                if (result.filter[1] && !this.isRefresh) {
                    logsRes = logs.concat(result.logs);
                } else {
                    logsRes = result.logs;
                }
                this.checkMore(logsRes);
                return logsRes;
            }, [])
        );
        this.isLoadMore$ = this.isLoadMoreSubject$.pipe();
    }

    mergeQueryParam(params: Record<string, unknown>): void {
        this.router.navigate([], {
            queryParams: params,
            queryParamsHandling: 'merge',
        });
    }

    private isLoadMore(value: [Filter, boolean, SortOrder]): boolean {
        return value[1] && !this.isRefresh && !!this.last;
    }

    private checkMore(logs: Log[]): void {
        this.isLoadMoreSubject$.next(logs ? logs.length < this.count : false);
    }
}
