import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login-component/login-component';
import { RegisterComponent } from './components/auth/register-component/register-component';
import { HomeComponent } from './components/home-component/home-component';
import { DashboardComponent } from './components/dashboard-component/dashboard-component';
import { AuthGuard } from './core/auth/auth.guard';
import { inject } from '@angular/core';
import { PlayerComponent } from './components/player-component/player-component';
import { ProfileComponent } from './components/profile-component/profile-component';
import { ChannelComponent } from './components/channel-component/channel-component';

export const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [() => inject(AuthGuard).canActivate()] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [() => inject(AuthGuard).canActivate()] },
  { path: 'player', component: PlayerComponent, canActivate: [() => inject(AuthGuard).canActivate()]},
  { path: 'profile', component: ProfileComponent, canActivate: [() => inject(AuthGuard).canActivate()]},
  { path: 'channel/:id', component: ChannelComponent, canActivate: [() => inject(AuthGuard).canActivate()] },
  { path: '**', redirectTo: '' },
];
