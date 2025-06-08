// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http'; // HttpClientModule is correctly here
import { DashboardComponent } from './dashboard/dashboard.component';
import { CartComponent } from './cart/cart.component';
import { ProductCategoryComponent } from './product-category/product-category.component';
import { ProductDetailsComponent } from './product-details/product-details.component'; // Keep this
import { PurchaseDetailsComponent } from './purchase-details/purchase-details.component';
import { AppUserComponent } from './app-user/app-user.component';
import { JwtModule } from '@auth0/angular-jwt';
import { AuthInterceptor } from './services/auth-interceptor.service';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { PaymentComponent } from './payment/payment.component';
import { ProfileComponent } from './profile/profile.component';
import { RegisterComponent } from './register/register.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { MediaComponent } from './media/media.component';



@NgModule({
  declarations: [
    AppComponent,
  
   
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
      MediaComponent,
    HttpClientModule, // Ensure this is present
    ReactiveFormsModule,
    FormsModule, // Ensure this is present
    AppUserComponent,
    RegisterComponent,
    ForgotPasswordComponent,
       CartComponent,
     PaymentComponent,
    DashboardComponent,
    ProductDetailsComponent, // Assuming this is standalone and imported correctly here
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatTableModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatFormFieldModule,
    MatInputModule,
    MatFormFieldModule,
    MatMenuModule,
    ProfileComponent,

    JwtModule.forRoot({
      config: {
        tokenGetter: () => {
          if (typeof window !== 'undefined' && window.localStorage) {
            const user = localStorage.getItem('user');
            return user ? JSON.parse(user).token : null;
          }
          return null;
        }
      }
    }),
    ProductCategoryComponent,
    AppUserComponent,
  ],
  providers: [
    provideClientHydration(withEventReplay()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }