import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { AsyncPipe } from '@angular/common';
import { ToastService } from './core/services/toast.service';

@Component({ selector: 'app-root', standalone: true, imports: [RouterOutlet, RouterLink, AsyncPipe], templateUrl: './app.component.html' })
export class AppComponent {
  theme: 'light' | 'dark' = 'light';
  constructor(public auth: AuthService, public toasts: ToastService) {
    this.theme = localStorage.getItem('oficiocr.theme') === 'dark' ? 'dark' : 'light';
    document.documentElement.dataset['theme'] = this.theme;
    this.auth.restoreSession();
  }
  toggleTheme(): void {
    this.theme = this.theme === 'light' ? 'dark' : 'light';
    document.documentElement.dataset['theme'] = this.theme;
    localStorage.setItem('oficiocr.theme', this.theme);
  }
}
