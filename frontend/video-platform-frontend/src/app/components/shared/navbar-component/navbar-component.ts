import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { UserService } from '../../../core/services/user-service';
import { Subject, takeUntil } from 'rxjs';
import { buildSafeUrl } from '../../../core/utils/safeUrl-util';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-navbar-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar-component.html',
  styleUrl: './navbar-component.css'
})
export class NavbarComponent implements OnInit, OnDestroy {
  query = '';
  isMobileSearchActive = false;
  avatarSafeUrl!: SafeResourceUrl;
  menuOpen = false;

  @Output() search = new EventEmitter<string>();

  private readonly avatarBase = 'http://localhost:8080/api/v1/users/avatars';
  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private sanitizer: DomSanitizer,
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userService.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.avatarSafeUrl = buildSafeUrl(this.sanitizer, this.avatarBase, user?.avatarUrl ?? null);
      });

    if (!this.userService.currentUser) {
      this.userService.loadUser().subscribe({ error: e => console.error(e) });
    } else {
      this.avatarSafeUrl = buildSafeUrl(
        this.sanitizer,
        this.avatarBase,
        this.userService.currentUser?.avatarUrl ?? null
      );
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  emitSearch() {
    if (this.query.trim().length > 0) {
      this.search.emit(this.query.trim());
    }
  }

  toggleMobileSearch() {
    this.isMobileSearchActive = !this.isMobileSearchActive;
  }

  goToUpload() {
    this.router.navigate(['/dashboard']);
  }

  goHome() {
    this.router.navigate(['/']);
  }

  goToProfile() {
  this.menuOpen = false;
  this.router.navigate(['/profile']);
}

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  logout() {
    this.menuOpen = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}