import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { ListComponent } from './components/list/list.component';
import { ListTableComponent } from './components/list-table/list-table.component';
import { ErrorComponent } from './components/error/error.component';
import { DetailComponent } from './components/detail/detail.component';
import { RecommendationsComponent } from './components/recommendations/recommendations.component';

const routes: Routes = [

  {path:"", component:HomeComponent},
  {path:"home", component:HomeComponent},
  {path: 'list', component: ListComponent },
  {path: 'list/:id', component: ListComponent },
  {path:"detail/:id", component:DetailComponent},
  {path:"list-table", component:ListTableComponent},
  {path:"list-table/:id", component:ListTableComponent},
  {path:"error", component:ErrorComponent},
  {path: 'recommendations', redirectTo: 'recommendations/ubicacion-actual', pathMatch: 'full' },
  {path: 'recommendations/:id', component: RecommendationsComponent },
  {path:"**", component:ErrorComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
