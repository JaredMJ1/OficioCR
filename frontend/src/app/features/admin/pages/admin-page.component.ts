import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { AdminUser } from '../../../core/models/admin-user.model';
import { AdminUserService } from '../services/admin-user.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastService } from '../../../core/services/toast.service';

@Component({ selector: 'app-admin-page', standalone: true, imports: [DatePipe, ReactiveFormsModule], templateUrl: './admin-page.component.html' })
export class AdminPageComponent {
  auth = inject(AuthService);
  private readonly userService = inject(AdminUserService);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(ToastService);
  operatorForm = this.fb.nonNullable.group({ name: ['', [Validators.required, Validators.maxLength(120)]], email: ['', [Validators.required, Validators.email]], password: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(128)]] });
  users: AdminUser[] = [];
  loading = false;
  error = '';
  createOperator(): void {
    if (this.operatorForm.invalid) { this.operatorForm.markAllAsTouched(); return; }
    this.userService.createOperator(this.operatorForm.getRawValue()).subscribe({
      next: response => { this.toast.show(response.message, 'success'); this.operatorForm.reset(); this.userService.list().subscribe(rows => this.users = rows); },
      error: () => undefined
    });
  }

  constructor() {
    this.auth.initialized$.subscribe(ready => {
      if (!ready) return;
      if (!this.auth.user) { this.auth.logout(); return; }
      if (this.auth.user.role !== 'superusuario') return;
      this.loading = true;
      this.userService.list().subscribe({
        next: rows => { this.users = rows; this.loading = false; },
        error: () => { this.error = 'No se pudo cargar la lista de usuarios.'; this.loading = false; }
      });
    });
  }
}

