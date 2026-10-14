import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    NgZone,
    OnDestroy,
    Output,
    ViewChild,
} from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';

import { InfiniteScrollTrigger } from '../infinite-scroll-trigger';

@Component({
    selector: 'fb-show-more-panel',
    templateUrl: 'show-more-panel.component.html',
    styleUrls: ['show-more-panel.component.scss'],
})
export class ShowMorePanelComponent implements AfterViewInit, OnDestroy {
    @ViewChild('sentinel', { static: true }) sentinel: ElementRef<HTMLElement>;

    @Input() set isLoading(value: boolean | null) {
        this.loading = Boolean(value);
        this.infiniteScrollTrigger.setLoading(this.loading);
    }

    get isLoading(): boolean {
        return this.loading;
    }

    @Output()
    showMore: EventEmitter<Params> = new EventEmitter();

    private loading = false;
    private readonly infiniteScrollTrigger: InfiniteScrollTrigger;

    constructor(private route: ActivatedRoute, private ngZone: NgZone) {
        this.infiniteScrollTrigger = new InfiniteScrollTrigger(() =>
            this.ngZone.run(() => this.showMore.emit(this.route.snapshot.queryParams))
        );
    }

    ngAfterViewInit() {
        this.infiniteScrollTrigger.observe(this.sentinel.nativeElement);
    }

    ngOnDestroy() {
        this.infiniteScrollTrigger.disconnect();
    }
}
