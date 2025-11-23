import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { HealthDashboardComponent } from './pages/health-dashboard/health-dashboard.component';
import { AuthGuard } from './auth/auth.guard';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { AdminGuard } from './auth/admin.guard';
import { UserAnalyticsComponent } from './pages/user-analytics/user-analytics.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  {
    path: 'dashboard',
    component: HealthDashboardComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'admin',
    component: AdminDashboardComponent, 
    canActivate: [AuthGuard, AdminGuard],
  },
{
    path: 'user-analytics/:id',
    component: UserAnalyticsComponent,
    canActivate: [AuthGuard, AdminGuard]
  },

  { path: '**', redirectTo: '' },
  


];
