import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth-service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register-component',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register-component.html',
  styleUrl: './register-component.css'
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  error = '';
  success = '';

  constructor(private authService: AuthService, private router: Router){}

  onSubmit(){
    const userData = {
      username:this.username,
      email: this.email,
      password: this.password
    };

    this.authService.register(userData).subscribe({
      next: () => {
        this.success = 'Successful sign up. You can now log in.';
        setTimeout(()=>this.router.navigate(['/login']), 2000)
      },
      error: (err) => {
        this.error = 'Error signing up. Please try again later.'
      },
    });

  }
}
