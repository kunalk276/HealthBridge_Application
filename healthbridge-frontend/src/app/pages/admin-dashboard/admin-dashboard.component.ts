import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { Router } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { AdminAnalyticsService } from '../../services/admin-analytics.service';
import { HealthDashboardService } from '../../services/health-dashboard.service';
import { AuthService } from '../../services/auth.service';
import { TranslateService } from '@ngx-translate/core';

Chart.register(...registerables);

interface AnalyticsData {
  userOverview: {
    totalUsers: number;
    usersByState: Record<string, number>;
    usersByCity: Record<string, number>;
    usersByLanguage: Record<string, number>;
    recentUsers: { id: number; username: string }[];
  };

  symptomTrends: {
    commonSymptoms: Record<string, number>;
    severityDistribution: Record<string, number>;
  };

  regionalHealth: {
    severityByState: Record<string, Record<string, number>>;
    highSeverityHotspots: string[];
  };

  aiAnalytics: {
    totalAiResponses: number;
    languageWisePerformance: Record<string, number>;
    referralStats: Record<string, number>;
  };
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, NgFor],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, AfterViewInit, OnDestroy {

  analytics: AnalyticsData | null = null;
  loading = true;

  @ViewChild('symptomChart') symptomChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('severityChart') severityChartRef!: ElementRef<HTMLCanvasElement>;

  private symptomChart: Chart | null = null;
  private severityChart: Chart | null = null;
  private viewReady = false;

  // NEW: Selected user analytics
  selectedUserId: number | null = null;
  selectedUserAnalytics: any = null;
  loadingUserAnalytics = false;

  constructor(
    private analyticsService: AdminAnalyticsService,
    private healthService: HealthDashboardService,
    private authService: AuthService,
    private translate: TranslateService,
    private router: Router
  ) {}

  // -------------------------------
  // FULL STATE NAMES + FULL LANGUAGE NAMES
  // -------------------------------

  private stateFullNames: Record<string, string> = {
    MH: 'Maharashtra',
    GJ: 'Gujarat',
    DL: 'Delhi',
    RJ: 'Rajasthan',
    MP: 'Madhya Pradesh',
    UP: 'Uttar Pradesh',
    UK: 'Uttarakhand',
    WB: 'West Bengal',
    TN: 'Tamil Nadu',
    KA: 'Karnataka',
    KL: 'Kerala',
    BR: 'Bihar',
    PB: 'Punjab',
    HR: 'Haryana',
    AP: 'Andhra Pradesh',
    TS: 'Telangana',
    OD: 'Odisha',
    CG: 'Chhattisgarh',
    AS: 'Assam',
    HP: 'Himachal Pradesh',
    JK: 'Jammu & Kashmir',
    GA: 'Goa',
    SK: 'Sikkim'
  };

  private languageFullNames: Record<string, string> = {
  en: 'English',
  hi: 'Hindi',
  mr: 'Marathi',
  gu: 'Gujarati',
  kn: 'Kannada',
  ml: 'Malayalam',
  ta: 'Tamil',
  te: 'Telugu',
  bn: 'Bengali',
  pa: 'Punjabi',
  or: 'Odia',
  ur: 'Urdu',
  as: 'Assamese'
};


  private replaceStateAbbreviations(input: Record<string, number>): Record<string, number> {
    const output: Record<string, number> = {};
    Object.keys(input).forEach(key => {
      const fullName = this.stateFullNames[key] || key;
      output[fullName] = input[key];
    });
    return output;
  }

  private replaceLanguageAbbreviations(input: Record<string, number>): Record<string, number> {
    const output: Record<string, number> = {};
    Object.keys(input).forEach(key => {
      const fullName = this.languageFullNames[key] || key;
      output[fullName] = input[key];
    });
    return output;
  }

  // -------------------------------
  // INIT
  // -------------------------------

  ngOnInit(): void {
    this.analyticsService.getAnalytics().subscribe({
      next: (res: any) => {
        this.analytics = res?.data;
        this.loading = false;
        if (!this.analytics) return;

        // 🔤 Normalize severity translations
        if (this.analytics.symptomTrends?.severityDistribution) {
          this.analytics.symptomTrends.severityDistribution =
            this.normalizeSeverity(this.analytics.symptomTrends.severityDistribution);
        }

        // 👤 Normalize recent users
        if (Array.isArray(this.analytics.userOverview?.recentUsers)) {
          this.analytics.userOverview.recentUsers =
            this.analytics.userOverview.recentUsers.map((u: any) => {
              if (typeof u === 'string') {
                return {
                  id: this.extractUserId(u),
                  username: u
                };
              }
              return u;
            });
        }

        // 🆕 Replace state + language abbreviations with full names
        if (this.analytics.userOverview) {
          this.analytics.userOverview.usersByState =
            this.replaceStateAbbreviations(this.analytics.userOverview.usersByState);

          this.analytics.userOverview.usersByLanguage =
            this.replaceLanguageAbbreviations(this.analytics.userOverview.usersByLanguage);
        }
if (this.analytics.aiAnalytics?.languageWisePerformance) {
  this.analytics.aiAnalytics.languageWisePerformance =
    this.replaceLanguageAbbreviations(
      this.analytics.aiAnalytics.languageWisePerformance
    );
}
        setTimeout(() => this.tryRenderCharts(), 150);
      },

      error: () => {
        this.loading = false;
        console.error('Failed to fetch analytics');
      }
    });

    
  }

  // -----------------------------------
  // LOAD USER ANALYTICS FOR ADMIN
  // -----------------------------------

  loadUserAnalyticsForAdmin(userId: number) {
    this.selectedUserId = userId;
    this.selectedUserAnalytics = null;
    this.loadingUserAnalytics = true;

    const username = localStorage.getItem('username');
    const password = localStorage.getItem('password');
    const headers = { Authorization: 'Basic ' + btoa(`${username}:${password}`) };

    this.healthService.getUserAnalytics(userId).then((res: any) => {
      this.loadingUserAnalytics = false;

      if (res?.success && res?.data) {
        this.selectedUserAnalytics = res.data;
      } else if (res?.totalRecords !== undefined) {
        this.selectedUserAnalytics = res;
      } else {
        console.warn('No analytics found for user', userId);
      }
    });
  }

  // -------------------------------
  // VIEW READY
  // -------------------------------

  ngAfterViewInit(): void {
    this.viewReady = true;
    setTimeout(() => this.tryRenderCharts(), 150);
  }

  ngOnDestroy(): void {
    if (this.symptomChart) this.symptomChart.destroy();
    if (this.severityChart) this.severityChart.destroy();
  }

  private tryRenderCharts(): void {
    if (!this.viewReady || !this.analytics) return;
    this.renderSymptomChart();
    this.renderSeverityChart();
  }

  // ------------------------------------
  // SEVERITY NORMALIZATION
  // ------------------------------------

  private normalizeSeverity(input: Record<string, number>): Record<string, number> {
    const severityMap: Record<string, 'HIGH' | 'MEDIUM' | 'LOW'> = {
      HIGH: 'HIGH', 'उच्च': 'HIGH', 'जास्त': 'HIGH', 'ઊંચું': 'HIGH',
      MEDIUM: 'MEDIUM', 'मध्यम': 'MEDIUM', 'मध्य': 'MEDIUM', 'મધ્યમ': 'MEDIUM',
      LOW: 'LOW', 'निम्न': 'LOW', 'कम': 'LOW', 'નીચું': 'LOW'
    };

    const result: Record<string, number> = { HIGH: 0, MEDIUM: 0, LOW: 0 };

    Object.keys(input).forEach(key => {
      const mapped = severityMap[key.trim()];
      if (mapped) result[mapped] += Number(input[key] ?? 0);
    });

    return result;
  }

  // ------------------------------------
  // CHARTS
  // ------------------------------------

  private renderSymptomChart(): void {
    if (!this.symptomChartRef?.nativeElement) return;
    if (this.symptomChart) this.symptomChart.destroy();

    const ctx = this.symptomChartRef.nativeElement.getContext('2d')!;
    const labels = Object.keys(this.analytics!.symptomTrends.commonSymptoms);
    const values = Object.values(this.analytics!.symptomTrends.commonSymptoms);

    if (labels.length === 0) return;

    const gradient = ctx.createLinearGradient(0, 0, 0, 300);
    gradient.addColorStop(0, 'rgba(59,130,246,0.95)');
    gradient.addColorStop(1, 'rgba(99,102,241,0.25)');

    this.symptomChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Symptom Frequency',
          data: values,
          backgroundColor: gradient,
          borderRadius: 10,
          borderSkipped: false
        }]
      },
      options: { responsive: true }
    });
  }

  private renderSeverityChart(): void {
    if (!this.severityChartRef?.nativeElement) return;
    if (this.severityChart) this.severityChart.destroy();

    const ctx = this.severityChartRef.nativeElement.getContext('2d')!;
    const dist = this.analytics!.symptomTrends.severityDistribution;

    this.severityChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['HIGH', 'MEDIUM', 'LOW'],
        datasets: [{
          label: 'Severity',
          data: [dist?.['HIGH'] ?? 0, dist?.['MEDIUM'] ?? 0, dist?.['LOW'] ?? 0],
          backgroundColor: ['#ef4444', '#f59e0b', '#10b981'],
          borderRadius: 10
        }]
      },
      options: { responsive: true }
    });
  }

  // ------------------------------------
  // USER NAVIGATION
  // ------------------------------------

  openUserAnalytics(id: number) {
    this.router.navigate(['/user-analytics', id]);
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  // internal utilities
  private extractUserId(username: string): number {
    return Math.abs(this.hashString(username)) % 100000;
  }

  private hashString(str: string): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = (hash << 5) - hash + str.charCodeAt(i);
      hash |= 0;
    }
    return hash;
  }

get formattedReferralStats() {
  const stats = this.analytics?.aiAnalytics?.referralStats || {};
  const result: any = {};

  for (const key in stats) {
    const value = stats[key];

    const normalized = String(value).toLowerCase();  // convert to string safely

    if (normalized === 'true' || normalized === '1') {
      result[key] = 'Yes';
    } else if (normalized === 'false' || normalized === '0') {
      result[key] = 'No';
    } else {
      result[key] = value;
    }
  }

  return result;
}

}
