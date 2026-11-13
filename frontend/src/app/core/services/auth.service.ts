import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private currentUser: User | null = null;
  private readonly initializedSubject = new BehaviorSubject(false);
  readonly initialized$ = this.initializedSubject.asObservable();

  constructor() { localStorage.removeItem('oficiocr.session'); }

  get user(): User | null { return this.currentUser; }

  register(data: { name: string; email: string; password: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/register`, data);
  }

  updateProfile(data: { name: string; email: string }): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/auth/me`, data).pipe(tap(user => { this.currentUser = user; }));
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(result => { this.currentUser = result.user; this.initializedSubject.next(true); })
    );
  }

  restoreSession(): void {
    if (this.initializedSubject.value) return;
    this.http.get<User>(`${environment.apiUrl}/auth/me`).subscribe({
      next: user => { this.currentUser = user; this.initializedSubject.next(true); },
      error: () => { this.currentUser = null; this.initializedSubject.next(true); }
    });
  }

  expireSession(): void {
    this.currentUser = null;
    this.initializedSubject.next(true);
    void this.router.navigateByUrl('/login');
  }

  logout(navigate = true): void {
    this.currentUser = null;
    this.http.post<void>(`${environment.apiUrl}/auth/logout`, null).subscribe({
      next: () => { if (navigate) void this.router.navigateByUrl('/login'); },
      error: () => { if (navigate) void this.router.navigateByUrl('/login'); }
    });
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/password/forgot`, { email });
  }
  resetPassword(email: string, token: string, password: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/password/reset`, { email, token, password });
  }
}
