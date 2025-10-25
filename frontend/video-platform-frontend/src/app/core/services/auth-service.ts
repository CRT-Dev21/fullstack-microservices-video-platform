import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { WebSocketService } from './websocket-service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/users';
  private tokenKey = 'jwt_token';
  private _isAuthenticated$ = new BehaviorSubject<boolean>(this.hasToken());
  isAuthenticated$ = this._isAuthenticated$.asObservable();

  constructor(private http: HttpClient, private wsService: WebSocketService) {}

  login(email: string, password: string): Observable<any> {
    return this.http
      .post<{ token: string }>(`${this.apiUrl}/login`, { email, password })
      .pipe(
        tap(res => {
          localStorage.setItem(this.tokenKey, res.token);
          this._isAuthenticated$.next(true);
          this.wsService.connect();
        })
      );
  }

  register(data: any) {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    this._isAuthenticated$.next(false);
    this.wsService.disconnect();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }
}
