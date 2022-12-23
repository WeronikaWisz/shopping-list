import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginComponent} from "./views/manage-users/login/login.component";
import {RegisterComponent} from "./views/manage-users/register/register.component";
import {ShoppingListComponent} from "./views/shopping-list/shopping-list.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'shopping-list', component: ShoppingListComponent },
  { path: '', redirectTo: 'shopping-list', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
