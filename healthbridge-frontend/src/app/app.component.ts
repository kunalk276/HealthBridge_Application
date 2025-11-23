import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  template: `
    <app-navbar></app-navbar>
    <router-outlet></router-outlet>
    <app-footer></app-footer>
  `
})
export class AppComponent implements OnInit {

  constructor(
    private translate: TranslateService,
    private authService: AuthService
  ) {
    translate.addLangs(['en', 'hi', 'mr', 'ta', 'te', 'bn', 'gu', 'pa', 'ml', 'kn']);
    translate.setDefaultLang('en');
  }

  ngOnInit(): void {
    const savedLang = this.authService.getUserLanguage() || 'en';
    this.translate.use(savedLang);
  }
}
