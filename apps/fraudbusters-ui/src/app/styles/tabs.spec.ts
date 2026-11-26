import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MAT_TABS_CONFIG, MatTabsModule } from '@angular/material/tabs';

@Component({
    imports: [MatTabsModule],
    template: `
        <nav mat-tab-nav-bar [tabPanel]="panel">
            <a mat-tab-link>Templates</a>
            <a mat-tab-link [active]="true">References <span class="fb-template-tab-count">7</span></a>
        </nav>
        <mat-tab-nav-panel #panel></mat-tab-nav-panel>
        <mat-tab-group [selectedIndex]="1">
            <mat-tab label="History"></mat-tab>
            <mat-tab label="Results"></mat-tab>
        </mat-tab-group>
    `,
})
class TabsFixtureComponent {}

describe('Tab highlight spacing', () => {
    it('keeps full-height highlights and aligns the first labels with the tab bar edge', async () => {
        TestBed.configureTestingModule({
            providers: [{ provide: MAT_TABS_CONFIG, useValue: { fitInkBarToContent: true, animationDuration: '0ms' } }],
        });
        const fixture = TestBed.createComponent(TabsFixtureComponent);
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();

        const host: HTMLElement = fixture.nativeElement;
        const tabs = host.querySelectorAll('.mdc-tab');
        expect(tabs.length).toBe(4);
        tabs.forEach((tab, index) => {
            expect(getComputedStyle(tab.querySelector('.mdc-tab__ripple')).display).toBe('none');
            expect(getComputedStyle(tab.querySelector('.mat-mdc-tab-ripple')).display).toBe('none');

            const label = tab.querySelector('.mdc-tab__content');
            const style = getComputedStyle(label);
            expect(style.paddingInlineStart).toBe(index % 2 === 0 ? '0px' : '5px');
            expect(style.paddingInlineEnd).toBe('5px');

            const paddedLabel = label.getBoundingClientRect();
            const tabBounds = tab.getBoundingClientRect();
            expect(paddedLabel.height).toBeCloseTo(tabBounds.height, 1);
            if (index % 2 === 0) {
                expect(paddedLabel.left).toBeCloseTo(tabBounds.left, 1);
            } else {
                const underline = label.querySelector('.mdc-tab-indicator__content--underline').getBoundingClientRect();
                expect(underline.left).toBeCloseTo(paddedLabel.left, 1);
                expect(underline.width).toBeCloseTo(paddedLabel.width, 1);
                expect(underline.bottom).toBeCloseTo(tabBounds.bottom, 1);
            }
        });
    });
});
