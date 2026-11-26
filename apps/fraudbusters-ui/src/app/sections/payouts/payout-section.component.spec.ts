import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { PayoutListsComponent } from './components/payout-lists/payout-lists.component';
import { PayoutSectionComponent } from './payout-section.component';
import { PayoutsModule } from './payouts.module';
import { ListType } from '../../api/fb-management/swagger-codegen/model/listType';
import { ConfigService } from '../../config';

describe(PayoutSectionComponent.name, () => {
    it('loads the Named tab alongside one metadata request and reuses totals when switching tabs', async () => {
        TestBed.configureTestingModule({
            imports: [PayoutsModule],
            providers: [
                provideRouter([
                    {
                        path: 'payouts/lists',
                        component: PayoutSectionComponent,
                        data: {
                            catalogSection: 'lists',
                            title: 'Lists',
                            links: [
                                { path: 'black', name: 'Black', listType: ListType.Black },
                                { path: 'named', name: 'Named', listType: ListType.Naming },
                            ],
                        },
                        children: [
                            { path: 'black', component: PayoutListsComponent, data: { listType: ListType.Black } },
                            { path: 'named', component: PayoutListsComponent, data: { listType: ListType.Naming } },
                        ],
                    },
                ]),
                provideHttpClient(),
                provideHttpClientTesting(),
                { provide: ConfigService, useValue: { fbManagementEndpoint: '/management' } },
            ],
        });
        const harness = await RouterTestingHarness.create('/payouts/lists/named');
        const http = TestBed.inject(HttpTestingController);
        const metadata = http.expectOne('/management/payouts/catalog/lists/metadata');
        const named = http.expectOne((request) => request.url === '/management/payouts-lists/filter');
        expect(named.request.params.get('listType')).toBe('naming');
        named.flush({ count: 0, result: [] });
        metadata.flush({
            counts: { black: 0, white: 0, grey: 0, named: 5 },
            listNames: { black: [], white: [], grey: [], named: ['example'] },
        });
        harness.detectChanges();
        expect(harness.routeNativeElement.querySelector('a[href$="/named"]').textContent).toContain('5');

        await harness.navigateByUrl('/payouts/lists/black');
        const black = http.expectOne((request) => request.url === '/management/payouts-lists/filter');
        expect(black.request.params.get('listType')).toBe('black');
        black.flush({ count: 0, result: [] });
        http.verify();
    });
});
