import { HttpParams } from '@angular/common/http';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const filterParameters = (params: any): HttpParams => {
    let searchParams = new HttpParams();
    if (params) {
        Object.keys(params).forEach((key) => {
            searchParams =
                params[key] !== undefined && params[key] !== null ? searchParams.set(key, params[key]) : searchParams;
        });
    }
    return searchParams;
};
