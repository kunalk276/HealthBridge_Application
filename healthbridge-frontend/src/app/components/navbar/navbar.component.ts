import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  selectedLang = 'en';
  langMenuOpen = false;
  menuOpen = false;

  languages = [
    { code: 'en', name: 'English',},
    { code: 'hi', name: 'हिंदी',  },
    { code: 'mr', name: 'मराठी',  },
    { code: 'ta', name: 'தமிழ்',  },
    { code: 'te', name: 'తెలుగు', },
    { code: 'bn', name: 'বাংলা',  },
    { code: 'gu', name: 'ગુજરાતી',  },
    { code: 'pa', name: 'ਪੰਜਾਬੀ',  },
    { code: 'ml', name: 'മലയാളം',},
    { code: 'kn', name: 'ಕನ್ನಡ',  },
  ];

  constructor(
    private translate: TranslateService,
    private authService: AuthService,
    private router: Router
  ) {
    translate.addLangs(this.languages.map(l => l.code));
    translate.setDefaultLang('en');
  }

  ngOnInit() {
    const savedLang = localStorage.getItem('lang') || 'en';
    this.selectedLang = savedLang;
    this.translate.use(savedLang);
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu() {
    this.menuOpen = false;
  }

  toggleLangMenu() {
    this.langMenuOpen = !this.langMenuOpen;
  }

  selectLanguage(lang: string) {
    this.selectedLang = lang;
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
    this.langMenuOpen = false;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get isAdmin(): boolean {
    return this.authService.getUserRole() === 'ROLE_ADMIN';
  }
}
