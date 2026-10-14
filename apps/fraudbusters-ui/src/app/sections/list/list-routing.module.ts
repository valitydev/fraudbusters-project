import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const ROUTES: Routes = [
    {
        path: 'black',
        loadChildren: () => import('./black-list').then((m) => m.BlackListModule),
    },
    {
        path: 'grey',
        loadChildren: () => import('./grey-list').then((m) => m.GreyListModule),
    },
    {
        path: 'white',
        loadChildren: () => import('./white-list').then((m) => m.WhiteListModule),
    },
];

@NgModule({
    imports: [RouterModule.forChild(ROUTES)],
    exports: [RouterModule],
})
export class ListRoutingModule {}
