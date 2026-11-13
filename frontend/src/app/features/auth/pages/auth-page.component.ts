import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({ selector: 'app-auth-page', standalone: true, imports: [ReactiveFormsModule, RouterLink], templateUrl: './auth-page.component.html' })
export class AuthPageComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);
  isRegister = inject(ActivatedRoute).snapshot.data['mode'] === 'register';
  showPassword = false;
  busy = false;
  error = '';
  success = '';

  form = this.fb.nonNullable.group({
    name: ['', this.isRegister ? [Validators.required, Validators.maxLength(120), Validators.pattern(/.*\S.*/)] : []],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254), Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/)]],
    password: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(128)]],
    confirmPassword: ['']
  });

  get password(): string { return this.form.controls.password.value; }
  get passwordRules(): { label: string; valid: boolean }[] {
    return [
      { label: 'Al menos 10 caracteres', valid: this.password.length >= 10 },
      { label: 'Una letra mayÃºscula', valid: /[A-Z]/.test(this.password) },
      { label: 'Una letra minÃºscula', valid: /[a-z]/.test(this.password) },
      { label: 'Un nÃºmero', valid: /\d/.test(this.password) },
      { label: 'Un sÃ­mbolo (por ejemplo, ! o #)', valid: /[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E]/.test(this.password) }
    ];
  }
  get passwordsMatch(): boolean { return this.form.controls.password.value === this.form.controls.confirmPassword.value; }

  submit(): void {
    this.error = '';
    this.success = '';
    const registerInvalid = this.isRegister && (
      this.form.controls.name.invalid ||
      this.passwordRules.some(rule => !rule.valid) || !this.passwordsMatch
    );
    if (this.form.controls.email.invalid || (this.isRegister ? registerInvalid : this.form.controls.password.invalid)) {
      this.form.markAllAsTouched();
      return;
    }

    this.busy = true;
    const value = this.form.getRawValue();
    if (this.isRegister) {
      this.auth.register({ name: value.name.trim(), email: value.email.trim().toLowerCase(), password: value.password }).subscribe({
        next: result => { this.success = result.message; this.toast.show(result.message, 'success'); this.busy = false; },
        error: error => this.fail(error)
      });
    } else {
      this.auth.login(value.email, value.password).subscribe({
        next: () => { this.toast.show('SesiÃ³n iniciada correctamente.', 'success'); this.busy = false; void this.router.navigateByUrl('/trabajos'); },
        error: error => this.fail(error)
      });
    }
  }

  private fail(error: HttpErrorResponse): void {
    this.busy = false;
    this.error = error.error?.message ?? 'No se pudo conectar con el servidor.';
  }
}
