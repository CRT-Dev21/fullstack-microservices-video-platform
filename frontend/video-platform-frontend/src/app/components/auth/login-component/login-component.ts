import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth-service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login-component',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css'
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';

  constructor(private authService: AuthService, private router:Router) {}

  onSubmit() {
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error = 'Invalid credentials';
        console.log(err)
      }
    })
  }
}
