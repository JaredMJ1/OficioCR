import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { JobListing } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  private http = inject(HttpClient);
  list(search = ''): Observable<JobListing[]> {
    return this.http.get<JobListing[]>(`${environment.apiUrl}/jobs`, { params: search ? { search } : {} });
  }
  create(job: Pick<JobListing, 'title' | 'description' | 'category' | 'location'>): Observable<JobListing> {
    return this.http.post<JobListing>(`${environment.apiUrl}/jobs`, job);
  }
  accept(id: string): Observable<JobListing> {
    return this.http.post<JobListing>(`${environment.apiUrl}/jobs/${id}/accept`, {});
  }
}
