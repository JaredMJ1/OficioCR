import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { JobService } from '../services/job.service';
import { JobListing } from '../models/job.model';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({ selector: 'app-jobs-page', standalone: true, imports: [ReactiveFormsModule, FormsModule, DatePipe], templateUrl: './jobs-page.component.html' })
export class JobsPageComponent {
  private jobs = inject(JobService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  auth = inject(AuthService);
  listings: JobListing[] = [];
  loading = false;
  search = '';
  showForm = false;
  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    category: ['', [Validators.required, Validators.maxLength(60)]],
    location: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  constructor() { this.load(); }

  load(): void {
    this.loading = true;
    this.jobs.list(this.search.trim()).subscribe({
      next: items => { this.listings = items; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  publish(): void {
    if (!this.auth.user) { this.toast.show('Inicia sesiÃ³n para publicar un anuncio.', 'info'); return; }
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.jobs.create(this.form.getRawValue()).subscribe({
      next: item => { this.listings = [item, ...this.listings]; this.form.reset(); this.showForm = false; this.toast.show('Tu anuncio ya estÃ¡ publicado.', 'success'); },
      error: (error: HttpErrorResponse) => this.toast.show(error.error?.message ?? 'No se pudo publicar el anuncio.', 'error')
    });
  }

  accept(item: JobListing): void {
    if (!this.auth.user) { this.toast.show('Inicia sesión para aceptar un trabajo.', 'info'); return; }
    this.jobs.accept(item.id).subscribe({
      next: updated => { this.listings = this.listings.map(listing => listing.id === updated.id ? updated : listing); this.toast.show('Has aceptado este trabajo.', 'success'); },
      error: (error: HttpErrorResponse) => this.toast.show(error.error?.message ?? 'No se pudo aceptar el trabajo.', 'error')
    });
  }
}

