import { Component, OnInit, OnDestroy } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter, Subject, takeUntil } from 'rxjs';
import { NotificationComponent } from './components/notification-component/notification-component';
import { NavbarComponent } from './components/shared/navbar-component/navbar-component';
import { WebSocketService } from './core/services/websocket-service';
import { AuthService } from './core/services/auth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, NotificationComponent, NavbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  showNavbar = true;
  notification: any;
  private destroy$ = new Subject<void>();

  constructor(
    private wsService: WebSocketService,
    private authService: AuthService,
    private router: Router
  ) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        const hiddenRoutes = ['/login', '/register'];
        this.showNavbar = !hiddenRoutes.includes(event.url);
      });
  }

  ngOnInit() {
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuth => {
        if (isAuth) {
          this.wsService.connect();
        } else {
          this.wsService.disconnect();
        }
      });

    this.wsService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe(msg => {
        if (!msg) return;
        this.notification = msg;
        setTimeout(() => (this.notification = null), 5000);
      });
  }

  onSearch(query: string) {
    if (query.trim()) {
      this.router.navigate(['/'], { queryParams: { q: query.trim() } });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
