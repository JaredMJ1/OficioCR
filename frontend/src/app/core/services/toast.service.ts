import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastKind = 'success' | 'error' | 'info';
export interface ToastMessage { id: number; kind: ToastKind; message: string; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  private sequence = 0;
  private readonly state = new BehaviorSubject<ToastMessage[]>([]);
  readonly messages$ = this.state.asObservable();

  show(message: string, kind: ToastKind = 'info'): void {
    const toast = { id: ++this.sequence, kind, message };
    this.state.next([...this.state.value, toast]);
    setTimeout(() => this.dismiss(toast.id), 5000);
  }

  dismiss(id: number): void { this.state.next(this.state.value.filter(item => item.id !== id)); }
}
