import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';

import { AppComponent } from './app.component';

describe('AppComponent', () => {
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RouterTestingModule, MatSnackBarModule],
            declarations: [AppComponent],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();
    });

    it('should create the app', () => {
        const fixture = TestBed.createComponent(AppComponent);
        const app = fixture.componentInstance;
        expect(app).toBeTruthy();
    });

    it('should render the application container and router outlet', () => {
        const fixture = TestBed.createComponent(AppComponent);
        fixture.detectChanges();
        const compiled = fixture.nativeElement;
        expect(compiled.querySelector('fb-container')).not.toBeNull();
        expect(compiled.querySelector('router-outlet')).not.toBeNull();
    });

    it('uses the active theme for the snackbar surface and draws a single border', () => {
        TestBed.createComponent(AppComponent).detectChanges();
        const snackbar = TestBed.inject(MatSnackBar).open('Test completed', 'OK');
        const container = document.querySelector('.mat-mdc-snack-bar-container');
        const surface = container.querySelector('.mat-mdc-snackbar-surface');
        const action = container.querySelector('.mat-mdc-snack-bar-action');
        const pane = container.closest('.cdk-overlay-pane');
        const root = document.documentElement;
        const wasDark = root.classList.contains('fb-dark');

        try {
            root.classList.remove('fb-dark');
            expect(getComputedStyle(surface).backgroundColor).toBe('rgb(255, 255, 255)');
            expect(getComputedStyle(surface).color).toBe('rgb(30, 41, 59)');
            expect(getComputedStyle(action).color).toBe('rgb(99, 56, 245)');
            expect(getComputedStyle(pane).borderTopWidth).toBe('0px');

            root.classList.add('fb-dark');
            expect(getComputedStyle(surface).backgroundColor).toBe('rgb(36, 35, 43)');
            expect(getComputedStyle(surface).color).toBe('rgb(238, 234, 245)');
            expect(getComputedStyle(action).color).toBe('rgb(201, 172, 255)');
            expect(getComputedStyle(surface).borderTopWidth).toBe('1px');
            expect(getComputedStyle(pane).borderTopWidth).toBe('0px');
        } finally {
            root.classList.toggle('fb-dark', wasDark);
            snackbar.dismiss();
        }
    });
});
