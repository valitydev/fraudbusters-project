export interface FetchAction<P> {
    type: 'search' | 'fetchMore';
    value?: P;
}
