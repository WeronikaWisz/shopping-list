import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {LoginRequest} from "../../models/auth/LoginRequest";
import {SignupRequest} from "../../models/auth/SignupRequest";

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  let testLoginRequest : LoginRequest = {
    password: "username",
    username: "password"
  }

  let testSignupRequest : SignupRequest = {
    email: "name@host.com",
    name: "name",
    password: "password",
    surname: "surname",
    username: "username"
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login should POST on signin', (done) => {
    service.login(testLoginRequest).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/auth/signin');
    expect(successRequest.request.method).toEqual('POST');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('register should POST on signup', (done) => {
    service.register(testSignupRequest).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/auth/signup');
    expect(successRequest.request.method).toEqual('POST');
    successRequest.flush(null);
    httpMock.verify();
  });
});
