import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MAT_TABS_CONFIG } from '@angular/material/tabs';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';

import { AppComponent } from './app.component';
import { ConfigModule, ConfigService } from './config';
import { ContainerModule } from './container';
import { initializer } from './initializer';
import { SectionsModule } from './sections/sections.module';
import { NavigateMenuModule } from './shared/components/navigate-menu/navigate-menu.module';

@NgModule({
    declarations: [AppComponent],
    bootstrap: [AppComponent],
    imports: [
        BrowserModule,
        ConfigModule,
        BrowserAnimationsModule,
        KeycloakAngularModule,
        NavigateMenuModule,
        SectionsModule,
        MatSidenavModule,
        MatIconModule,
        RouterModule,
        MatButtonModule,
        MatMenuModule,
        MatDividerModule,
        ContainerModule,
    ],
    providers: [
        {
            provide: APP_INITIALIZER,
            useFactory: initializer,
            multi: true,
            deps: [ConfigService, KeycloakService],
        },
        provideHttpClient(withInterceptorsFromDi()),
        // Catalog tabs start at the left edge; an ink bar under the label only lines up with it.
        { provide: MAT_TABS_CONFIG, useValue: { fitInkBarToContent: true } },
    ],
})
export class AppModule {}
