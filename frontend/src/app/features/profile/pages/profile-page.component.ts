import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({ selector: 'app-profile-page', standalone: true, imports: [ReactiveFormsModule], templateUrl: './profile-page.component.html' })
export class ProfilePageComponent {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  saving = false;
  form = this.fb.nonNullable.group({ name: ['', [Validators.required, Validators.maxLength(120)]], email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]] });

  constructor() {
    const user = this.auth.user;
    if (user) this.form.patchValue({ name: user.name, email: user.email });
    else this.auth.initialized$.subscribe(ready => {
      if (ready && this.auth.user) this.form.patchValue({ name: this.auth.user.name, email: this.auth.user.email });
    });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.auth.updateProfile({ name: this.form.controls.name.value.trim(), email: this.form.controls.email.value.trim() }).subscribe({
      next: () => { this.saving = false; this.toast.show('Tus datos se actualizaron correctamente.', 'success'); },
      error: (error: HttpErrorResponse) => { this.saving = false; this.toast.show(error.error?.message ?? 'No se pudieron guardar tus datos.', 'error'); }
    });
  }
}
