export interface FetchResultContinuation<T> {
    result?: T[];
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    continuationId?: any;
}
