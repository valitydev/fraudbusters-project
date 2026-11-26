import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { GroupsComponent } from './groups.component';
import { GroupsModule } from './groups.module';
import { ConfigService } from '../../config';

describe(GroupsComponent.name, () => {
    it('shows compact zero badges before the counts arrive and sizes badges to their values', () => {
        TestBed.configureTestingModule({
            imports: [GroupsModule],
            providers: [
                provideRouter([]),
                provideHttpClient(),
                provideHttpClientTesting(),
                { provide: ConfigService, useValue: { fbManagementEndpoint: '/management' } },
            ],
        });
        const fixture = TestBed.createComponent(GroupsComponent);
        const page: HTMLElement = fixture.nativeElement;
        fixture.detectChanges();

        const tabs = Array.from(page.querySelectorAll('nav a'));
        const counts = tabs.map((tab) => tab.querySelector('.fb-template-tab-count'));
        const initialWidth = counts[0].getBoundingClientRect().width;
        expect(counts.map((count) => count.textContent.trim())).toEqual(['0', '0']);

        const http = TestBed.inject(HttpTestingController);
        http.expectOne('/management/payments/catalog/groups/metadata').flush({
            counts: { groups: 2147483647, references: 12345 },
        });
        fixture.detectChanges();

        expect(counts.map((count) => count.textContent.trim())).toEqual(['2147483647', '12345']);
        expect(counts[0].getBoundingClientRect().width).toBeGreaterThan(initialWidth);
        http.verify();
    });
});
