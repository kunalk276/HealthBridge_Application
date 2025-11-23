import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service'; 

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  fullText = 'AI Healthcare for Every Indian, in Every Language.';
  displayedText = '';
  isDarkMode = false;

  features = [
    {
      title: 'AI Symptom Checker',
      icon: 'fa-solid fa-stethoscope',
      desc: 'Get instant AI-backed symptom insights and next-step suggestions.'
    },
    {
      title: 'Multilingual Conversations',
      icon: 'fa-solid fa-language',
      desc: 'Speak in your preferred language — Hindi, Marathi, Tamil, or English.'
    },
    {
      title: 'Voice-to-AI Consultation',
      icon: 'fa-solid fa-microphone',
      desc: 'Talk naturally. Our AI listens and understands medical terms instantly.'
    },
    {
      title: 'Real-time Data Insights',
      icon: 'fa-solid fa-chart-line',
      desc: 'Analyze health patterns and receive data-driven suggestions.'
    },
    {
      title: 'Community Health Hub',
      icon: 'fa-solid fa-people-group',
      desc: 'Access collective health trends and share wellness updates.'
    },
    {
      title: 'Personalized Health Feed',
      icon: 'fa-solid fa-heart-pulse',
      desc: 'AI-curated recommendations to improve your lifestyle and wellbeing.'
    }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.startTypewriterEffect();
    this.initializeDarkMode();
  }

  startTypewriterEffect() {
    let i = 0;
    const speed = 50;
    const type = () => {
      if (i < this.fullText.length) {
        this.displayedText += this.fullText.charAt(i);
        i++;
        setTimeout(type, speed);
      }
    };
    type();
  }

  initializeDarkMode() {
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode =
      savedTheme === 'dark' ||
      (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches);
    document.documentElement.classList.toggle('dark', this.isDarkMode);
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
goToDashboard() {
    if (this.isLoggedIn) {
      this.router.navigate(['/dashboard']);
    }
  }
  
}
