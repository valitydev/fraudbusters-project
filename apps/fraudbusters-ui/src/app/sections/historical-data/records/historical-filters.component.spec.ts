import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';

import { HISTORY_PERIODS, HistoricalFiltersComponent } from './historical-filters.component';
import { HISTORICAL_KINDS, HistoricalFilters } from './historical-kinds';

describe('HistoricalFiltersComponent', () => {
    const create = (query: Record<string, string>, kind = HISTORICAL_KINDS.payments) => {
        const navigate = jasmine.createSpy('navigate').and.resolveTo(true);
        TestBed.configureTestingModule({
            imports: [HistoricalFiltersComponent],
            providers: [
                { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap(query) } } },
                { provide: Router, useValue: { navigate } },
            ],
        });
        const fixture = TestBed.createComponent(HistoricalFiltersComponent);
        fixture.componentRef.setInput('kind', kind);
        const emitted: HistoricalFilters[] = [];
        fixture.componentInstance.filtersChange.subscribe((filters) => emitted.push(filters));
        fixture.detectChanges();
        return { component: fixture.componentInstance, emitted, navigate };
    };

    it('opens on the last day with the 1 day preset selected', () => {
        const { component, emitted } = create({});
        const day = HISTORY_PERIODS.find((period) => period.label === '1 day').duration;
        expect(component.isPeriod(day)).toBeTrue();
        expect(component.isCustomPeriod).toBeFalse();
        expect(emitted.length).toBe(1);
    });

    it('reads old parameter names and keeps additional filters open when one is set', () => {
        const { component, emitted } = create({ shopId: 'shop-1', paymentId: 'p1', cardToken: 'tok' });
        expect(emitted[0]).toEqual(jasmine.objectContaining({ scopeId: 'shop-1', id: 'p1', cardToken: 'tok' }));
        expect(component.additionalOpen).toBeTrue();
        expect(component.additionalCount).toBe(1);
    });

    it('sends only the filters the tab shows', () => {
        const { emitted } = create({ template: 'tpl', partyId: 'party' }, HISTORICAL_KINDS.payments);
        expect(emitted[0].partyId).toBe('party');
        expect(emitted[0].template).toBeUndefined();
    });

    it('marks a manual period as custom', () => {
        const { component } = create({ from: '2026-01-01T00:00:00Z', to: '2026-01-03T05:00:00Z' });
        expect(component.isCustomPeriod).toBeTrue();
    });
});
