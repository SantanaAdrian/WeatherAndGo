import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { ListComponent } from './components/list/list.component';
import { ListTableComponent } from './components/list-table/list-table.component';
import { ErrorComponent } from './components/error/error.component';
import { DetailComponent } from './components/detail/detail.component';
import { RecommendationsComponent } from './components/recommendations/recommendations.component';

const routes: Routes = [
  { path: '', redirectTo: 'home/ubicacion-actual', pathMatch: 'full' },

  { path: 'home', redirectTo: 'home/ubicacion-actual', pathMatch: 'full' },
  { path: 'home/:id', component: HomeComponent },

  { path: 'list', redirectTo: 'list/ubicacion-actual', pathMatch: 'full' },
  { path: 'list/:id', component: ListComponent },

  { path: 'detail/:id', component: DetailComponent },

  { path: 'list-table', redirectTo: 'list-table/ubicacion-actual', pathMatch: 'full' },
  { path: 'list-table/:id', component: ListTableComponent },

  { path: 'recommendations', redirectTo: 'recommendations/ubicacion-actual', pathMatch: 'full' },
  { path: 'recommendations/:id', component: RecommendationsComponent },

  { path: 'error', component: ErrorComponent },
  { path: '**', component: ErrorComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
