import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MainNavComponent } from './main-nav/main-nav.component';
import {authInterceptorProviders} from "./helpers/auth.interceptor";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import { LoginComponent } from './views/manage-users/login/login.component';
import { RegisterComponent } from './views/manage-users/register/register.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatListModule} from "@angular/material/list";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import { MatButtonModule } from '@angular/material/button'
import {MatStepperModule} from "@angular/material/stepper";
import { ShoppingListComponent } from './views/shopping-list/shopping-list.component';
import {ChangePasswordDialogComponent} from "./views/manage-users/change-password-dialog/change-password-dialog.component";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTabsModule} from "@angular/material/tabs";
import {MatRippleModule} from "@angular/material/core";
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
// import { LayoutModule } from '@angular/cdk/layout';
import {MatDialogModule} from "@angular/material/dialog";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatDatepickerModule} from "@angular/material/datepicker";
import { MatNativeDateModule } from '@angular/material/core';
// import { MatMomentDateModule } from "@angular/material-moment-adapter";
// import {MatChipsModule} from "@angular/material/chips";
// import {MatAutocompleteModule} from "@angular/material/autocomplete";
// import {MatTooltipModule} from "@angular/material/tooltip";
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { ImageDialogComponent } from './views/image-dialog/image-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    MainNavComponent,
    LoginComponent,
    RegisterComponent,
    ShoppingListComponent,
    ChangePasswordDialogComponent,
    ImageDialogComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        HttpClientModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        BrowserAnimationsModule,
        MatSidenavModule,
        MatToolbarModule,
        MatListModule,
        MatIconModule,
        MatMenuModule,
        MatCardModule,
        MatFormFieldModule,
        ReactiveFormsModule,
        MatInputModule,
        MatButtonModule,
        MatStepperModule,
        MatPaginatorModule,
        MatTabsModule,
        MatRippleModule,
        FormsModule,
        MatDialogModule,
        MatCheckboxModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule
    ],
  providers: [authInterceptorProviders,
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB' }],
  bootstrap: [AppComponent]
})
export class AppModule { }

export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}
