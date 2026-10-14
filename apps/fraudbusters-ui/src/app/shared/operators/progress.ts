import { merge, Observable, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, startWith } from 'rxjs/operators';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const progress = (start$: Observable<any>, end$: Observable<any>, startValue = false): Observable<boolean> =>
    merge(start$.pipe(map(() => true)), end$.pipe(map(() => false))).pipe(
        catchError(() => of(false)),
        startWith(startValue),
        distinctUntilChanged()
    );
