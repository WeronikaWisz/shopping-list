import { Injectable } from '@angular/core';
import {SignupRequest} from "../../models/auth/SignupRequest";
import {LoginRequest} from "../../models/auth/LoginRequest";
import {Observable} from "rxjs";
import {HttpClient, HttpHeaders} from "@angular/common/http";

const AUTH_API = 'http://localhost:8080/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest): Observable<any> {
    return this.http.post(AUTH_API + 'signin', JSON.stringify(loginRequest), httpOptions);
  }

  register(signupRequest: SignupRequest): Observable<any> {
    console.log(signupRequest)
    return this.http.post(AUTH_API + 'signup', JSON.stringify(signupRequest), httpOptions);
  }

}
