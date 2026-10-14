export class InfiniteScrollTrigger {
    private observer?: IntersectionObserver;
    private retryTimer?: number;
    private isIntersecting = false;
    private isLoading = false;
    private requestPending = false;

    constructor(private readonly loadMore: () => void) {}

    observe(element: Element) {
        this.observer = new IntersectionObserver(
            ([entry]) => {
                this.isIntersecting = entry.isIntersecting;
                this.loadIfNeeded();
            },
            { rootMargin: '300px 0px' }
        );
        this.observer.observe(element);
    }

    setLoading(isLoading: boolean) {
        const requestFinished = this.isLoading && !isLoading;
        this.isLoading = isLoading;
        if (requestFinished) {
            this.requestPending = false;
            this.retryTimer = window.setTimeout(() => this.loadIfNeeded());
        }
    }

    disconnect() {
        this.observer?.disconnect();
        if (this.retryTimer) {
            window.clearTimeout(this.retryTimer);
        }
    }

    private loadIfNeeded() {
        if (!this.isIntersecting || this.isLoading || this.requestPending) {
            return;
        }
        this.requestPending = true;
        this.loadMore();
    }
}
