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
import {SignupRequest} from "../../../models/auth/SignupRequest";

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

describe('RegisterComponent integration test with AuthService', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let service: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegisterComponent ],
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ]
    })
      .compileComponents();
    service = TestBed.inject(AuthService)
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('register user', () => {

    it('updateShoppingListButton should call service register', () => {
      component.form.controls['formArray'].get([0])?.get('email')?.setValue('email@host.com')
      component.form.controls['formArray'].get([0])?.get('name')?.setValue('name')
      component.form.controls['formArray'].get([0])?.get('surname')?.setValue('surname')
      component.form.controls['formArray'].get([1])?.get('password')?.setValue('passwordpassword')
      component.form.controls['formArray'].get([1])?.get('username')?.setValue('username')
      component.form.controls['formArray'].get([1])?.get('confirmPassword')?.setValue('passwordpassword')

      fixture.detectChanges()

      let request : SignupRequest = {
        "username": 'username',
        "email": 'email@host.com',
        "password": 'passwordpassword',
        "name": 'name',
        "surname": 'surname'
      }

      let spy = spyOn(service, 'register').withArgs(request).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#submit-register-button')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(request)
    });
  });

});
