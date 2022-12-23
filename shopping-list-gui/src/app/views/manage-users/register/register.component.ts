import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../services/manage-users/auth.service';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import Validation from '../../../helpers/validation';
import {STEPPER_GLOBAL_OPTIONS} from '@angular/cdk/stepper';
import {Router} from "@angular/router";
import {TokenStorageService} from "../../../services/token-storage.service";
import Swal from 'sweetalert2';
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.sass'],
  providers: [{
    provide: STEPPER_GLOBAL_OPTIONS, useValue: {showError: true}
  }]
})
export class RegisterComponent implements OnInit {

  form!: FormGroup;
  isLoggedIn = false;
  hide = true;

  get formArray(): AbstractControl | null { return this.form.get('formArray'); }

  constructor(private formBuilder: FormBuilder, private authService: AuthService, private translate: TranslateService,
              private router: Router, private tokenStorage: TokenStorageService) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      formArray: this.formBuilder.array([
        this.formBuilder.group({
          name: ['', Validators.required],
          surname: ['', Validators.required],
          email: ['', [Validators.required, Validators.email]]
        }),
        this.formBuilder.group({
            username: [
              '',
              [
                Validators.required,
                Validators.minLength(6),
                Validators.maxLength(20)
              ]
            ],
            password: [
              '',
              [
                Validators.required,
                Validators.minLength(12),
                Validators.maxLength(40)
              ]
            ],
            confirmPassword: ['', Validators.required]
          },
          {
            validators: [Validation.match('password', 'confirmPassword')]
          }
        )
      ])
    });
    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
      this.router.navigate(['/shopping-list']).then(() => this.reloadPage());
    }
  }

  onSubmit(): void {

    this.authService.register({
      "username": this.formArray!.get([1])!.get('username')?.value,
      "email": this.formArray!.get([0])!.get('email')?.value,
      "password": this.formArray!.get([1])!.get('password')?.value,
      "name": this.formArray!.get([0])!.get('name')?.value,
      "surname": this.formArray!.get([0])!.get('surname')?.value
    }).subscribe(
      data => {
        console.log(data);
        this.router.navigate(['/login']).then(() => this.showSuccess());
      },
      err => {
        if(err.error.message.includes("e-mail")){
          this.form.controls['formArray'].get([0])?.get('email')?.setErrors({'incorrect': true})
        } else if(err.error.message.includes("Nazwa") || err.error.message.includes("Username")){
          this.form.controls['formArray'].get([1])?.get('username')?.setErrors({'incorrect': true})
        }
        Swal.fire({
          position: 'top-end',
          title: this.getTranslateMessage("manage-users.register.register-error"),
          text: err.error.message,
          icon: 'error',
          showConfirmButton: false
        })
      }
    );
  }

  showSuccess(): void {
    Swal.fire({
      position: 'top-end',
      title: this.getTranslateMessage("manage-users.register.register-success"),
      text: this.getTranslateMessage("manage-users.register.register-success-can-login"),
      icon: 'success',
      showConfirmButton: false,
      timer: 6000
    })
  }

  reloadPage(): void {
    window.location.reload();
  }

  getTranslateMessage(key: string): string{
    let message = "";
    this.translate.get(key).subscribe(data =>
      message = data
    );
    return message;
  }

}
