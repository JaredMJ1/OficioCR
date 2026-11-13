import { Routes } from '@angular/router';
import { AuthPageComponent } from './features/auth/pages/auth-page.component';
import { AdminPageComponent } from './features/admin/pages/admin-page.component';
import { JobsPageComponent } from './features/jobs/pages/jobs-page.component';
import { ProfilePageComponent } from './features/profile/pages/profile-page.component';

export const routes: Routes = [
  { path: 'registro', component: AuthPageComponent, data: { mode: 'register' } },
  { path: 'login', component: AuthPageComponent, data: { mode: 'login' } },
  { path: 'admin', component: AdminPageComponent },
  { path: 'trabajos', component: JobsPageComponent },
  { path: 'perfil', component: ProfilePageComponent },
  { path: '', pathMatch: 'full', redirectTo: 'trabajos' },
  { path: '**', redirectTo: 'trabajos' }
];
