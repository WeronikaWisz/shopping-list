import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterComponent } from './register.component';
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {TranslateModule} from "@ngx-translate/core";
import {of, throwError} from "rxjs";
import {TokenStorageService} from "../../../services/token-storage.service";
import {AuthService} from "../../../services/manage-users/auth.service";
import Swal from "sweetalert2";

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  let testTokenStorageService = jasmine.createSpyObj(['getToken'])

  let testAuthService = jasmine.createSpyObj(['register'])

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegisterComponent ],
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: TokenStorageService, useValue: testTokenStorageService },
        { provide: AuthService, useValue: testAuthService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getToken on tokenStorageService', () => {
    expect(testTokenStorageService.getToken).toHaveBeenCalled();
  });

  it('should set loggedIn false if token not set', () => {
    expect(component.isLoggedIn).toBeFalse()
  });

  // it('should set loggedIn if token already set', () => {
  //   testTokenStorageService.getToken.and.returnValue(of('sometoken'))
  //   expect(component.isLoggedIn).toBeTruthy()
  // });

  it('onSubmit should call authService register', () => {
    testAuthService.register.and.returnValue(of("data"))
    component.onSubmit()
    expect(testAuthService.register).toHaveBeenCalled()
  });

  it('onSubmit should show email error while error message contains e-mail from authService', () => {
    testAuthService.register.and.returnValue(throwError({error: {status: 404, message: 'error e-mail'}}))
    spyOn(Swal,"fire").and.stub();
    component.onSubmit()
    expect(component.form.controls['formArray'].get([0])?.get('email')?.errors?.incorrect).toBeTruthy()
  });

  it('onSubmit should show email error while error message contains Username from authService', () => {
    testAuthService.register.and.returnValue(throwError({error: {status: 404, message: 'error Username'}}))
    spyOn(Swal,"fire").and.stub();
    component.onSubmit()
    expect(component.form.controls['formArray'].get([1])?.get('username')?.errors?.incorrect).toBeTruthy()
  });

});
