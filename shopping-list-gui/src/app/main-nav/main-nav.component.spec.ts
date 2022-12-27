import { LayoutModule } from '@angular/cdk/layout';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';

import { MainNavComponent } from './main-nav.component';
import {TranslateModule} from "@ngx-translate/core";
import {RouterTestingModule} from "@angular/router/testing";
import {MatMenuModule} from "@angular/material/menu";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {TokenStorageService} from "../services/token-storage.service";
import {of} from "rxjs";
import {Router} from "@angular/router";
import {ChangePasswordDialogComponent} from "../views/manage-users/change-password-dialog/change-password-dialog.component";
import {InjectionToken} from "@angular/core";

// export const WINDOW = new InjectionToken('window');

describe('MainNavComponent', () => {
  let component: MainNavComponent;
  let fixture: ComponentFixture<MainNavComponent>;

  let testUser = {
    username: "username",
    roles: ['USER']
  }

  // const thenable = {
  //   then(onFulfilled: any) {
  //     onFulfilled("Resolving");
  //     throw new TypeError("Throwing");
  //   },
  // };

  let testTokenStorageService = jasmine.createSpyObj(['getUser', 'getToken', 'signOut'])
  testTokenStorageService.getToken.and.returnValue(of('sometoken'))
  testTokenStorageService.getUser.and.returnValue(of(testUser))

  let testMatDialog = jasmine.createSpyObj(['open'])

  // let windowMock = {
  //   location: {
  //     reload: jasmine.createSpy('reload')
  //   }
  // }

  // let testRouter = jasmine.createSpyObj(['navigate'])
  // testRouter.navigate.and.returnValue(of(Promise.resolve(thenable)))
  // testRouter.navigate.and.callFake(() => Promise.resolve("Success"))

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MainNavComponent ],
      imports: [
        NoopAnimationsModule,
        LayoutModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatSidenavModule,
        MatToolbarModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
        MatMenuModule,
        MatDialogModule
      ],
      providers: [
        { provide: TokenStorageService, useValue: testTokenStorageService },
        { provide: MatDialog, useValue: testMatDialog},
        // { provide: WINDOW, useValue: windowMock}
        // { provide: Router, useValue: testRouter}
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MainNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getToken on tokenStorageService', () => {
    expect(testTokenStorageService.getToken).toHaveBeenCalled();
  });

  it('should call getUser on tokenStorageService', () => {
    expect(testTokenStorageService.getToken).toHaveBeenCalled();
  });

  it('logout should call signOut on tokenStorageService', () => {
    component.logout()
    expect(testTokenStorageService.signOut).toHaveBeenCalled();
  });

  // it('logout should redirect to login component', () => {
    // let spy = spyOn(component, 'reloadPage').and.callThrough();
  //   component.logout()
  //   expect(testRouter.navigate).toHaveBeenCalledWith(['/login']);
  // });

  it('changePassword should open ChangePasswordDialogComponent', () => {
    component.changePassword()
    expect(testMatDialog.open).toHaveBeenCalled();
  });

  // it('reloadPage should call window location reload', () => {
  //   component.reloadPage()
  //   expect(windowMock.location.reload).toHaveBeenCalled();
  // });

});
