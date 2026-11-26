import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { BlackListComponent } from './components/black-list/black-list.component';
import { ListsComponent } from './lists.component';
import { ListsModule } from './lists.module';
import { ConfigService } from '../../config';

describe(ListsComponent.name, () => {
    it('requests the first list and metadata independently and shows the counts before the rows', async () => {
        TestBed.configureTestingModule({
            imports: [ListsModule],
            providers: [
                provideRouter([
                    {
                        path: 'lists',
                        component: ListsComponent,
                        children: [{ path: 'black', component: BlackListComponent }],
                    },
                ]),
                provideHttpClient(),
                provideHttpClientTesting(),
                { provide: ConfigService, useValue: { fbManagementEndpoint: '/management', pageSize: 10 } },
            ],
        });
        const harness = await RouterTestingHarness.create('/lists/black');
        const page = harness.routeNativeElement;
        const content = page.querySelector<HTMLElement>('fb-catalog-metadata-content > div');
        const http = TestBed.inject(HttpTestingController);
        const metadata = http.expectOne('/management/payments/catalog/lists/metadata');
        expect(content.hidden).toBeTrue();

        const records = http.expectOne(
            (request) => request.url === '/management/payments/catalog/lists/filter-records'
        );
        expect(records.request.params.get('listType')).toBe('black');
        expect(records.request.params.has('listNames')).toBeFalse();
        records.flush({ count: 1, result: [{ id: 'record', listName: 'example', value: 'example-record' }] });
        harness.detectChanges();

        expect(content.hidden).toBeTrue();
        metadata.flush({
            counts: { black: 1, white: 0, grey: 0, candidateBatches: 0 },
            listNames: { black: ['example'], white: [], grey: [] },
        });
        harness.detectChanges();
        expect(page.querySelector('.fb-template-tab-count').textContent.trim()).toBe('1');
        expect(content.hidden).toBeTrue();

        await new Promise<void>((resolve) =>
            requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(() => resolve())))
        );
        harness.detectChanges();
        expect(content.hidden).toBeFalse();
        expect(content.textContent).toContain('example-record');
        http.verify();
    });
});
