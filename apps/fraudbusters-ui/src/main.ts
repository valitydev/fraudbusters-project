import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { ENVIRONMENT } from './environments/environment';

try {
    document.documentElement.classList.toggle('fb-dark', localStorage.getItem('fb-theme') === 'dark');
} catch {
    // Start with the default light theme when browser storage is unavailable.
}

if (ENVIRONMENT.production) {
    enableProdMode();
}

platformBrowserDynamic()
    .bootstrapModule(AppModule)
    .catch((err) => console.error(err));
