import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { AuthGuard } from './auth.guard';
import { AppUserComponent } from './app-user/app-user.component';
import { CartComponent } from './cart/cart.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductCategoryComponent } from './product-category/product-category.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { PurchaseDetailsComponent } from './purchase-details/purchase-details.component';
import { PaymentComponent } from './payment/payment.component';
import { ProfileComponent } from './profile/profile.component';
import { RegisterComponent } from './register/register.component'; // <--- NEW IMPORT
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component'; // <--- NEW IMPORT
import { MediaComponent } from './media/media.component';


const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'login', component: LoginComponent }, // <--- GOOD PRACTICE TO EXPLICITLY DEFINE /login
  { path: 'register', component: RegisterComponent }, // <--- NEW ROUTE
  { path: 'forgot-password', component: ForgotPasswordComponent }, // <--- NEW ROUTE
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'users', component: AppUserComponent, canActivate: [AuthGuard] },
  { path: 'product-categories', component: ProductCategoryComponent, canActivate: [AuthGuard] },
  { path: 'products', component: ProductDetailsComponent, canActivate: [AuthGuard] },
  { path: 'purchases', component: PurchaseDetailsComponent, canActivate: [AuthGuard] },
  { path: 'payment/:type/:id', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
   { path: 'media', component: MediaComponent },

  { path: '**', redirectTo: '' }, // Fallback to login
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }