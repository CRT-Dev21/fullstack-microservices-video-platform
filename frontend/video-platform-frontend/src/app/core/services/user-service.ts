import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface User {
  username: string;
  avatarUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/users'
  private userSubject = new BehaviorSubject<User | null>(null);

  user$: Observable<User | null> = this.userSubject.asObservable();
  constructor(private http: HttpClient) {}

  loadUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => this.userSubject.next(user))
    );
  }

  refreshUser(): void {
    this.http.get<User>(`${this.apiUrl}/me`).subscribe({
      next: (user) => this.userSubject.next(user)
    });
  }

  get currentUser(): User | null {
    return this.userSubject.value;
  }

  updateAvatar(avatarUrl: string): void {
    const user = this.userSubject.value;
    if (user) {
      this.userSubject.next({ ...user, avatarUrl });
    }
  }

  logout(): void {
    localStorage.removeItem("jwt_token");
  }

}
