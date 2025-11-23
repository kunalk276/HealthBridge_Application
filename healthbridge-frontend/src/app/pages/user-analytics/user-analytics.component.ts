import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { HealthDashboardService } from '../../services/health-dashboard.service';

Chart.register(...registerables);

@Component({
  selector: 'app-user-analytics',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './user-analytics.component.html',
  styleUrls: ['./user-analytics.component.css']
})
export class UserAnalyticsComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('symptomChart') symptomChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('severityChart') severityChartRef!: ElementRef<HTMLCanvasElement>;

  userId: number | null = null;
  userName = '';
  loading = true;
  analytics: any = null;
  private symptomChart: Chart | null = null;
  private severityChart: Chart | null = null;
  private viewReady = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private healthService: HealthDashboardService
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    this.userId = param ? Number(param) : null;
    if (!this.userId) {
      console.warn('No user id in route');
      this.loading = false;
      return;
    }
    this.loadAnalytics();
  }

  async loadAnalytics() {
    try {
      this.loading = true;
      const res: any = await this.healthService.getUserAnalytics(this.userId!);
      // backend may wrap in { success, data } or return raw object
      this.analytics = res?.data ?? res ?? null;
      this.userName = this.analytics?.username || this.analytics?.userName || 'User';
      // normalize severity keys if needed (strings in other languages) -> simple mapping
      if (this.analytics?.severityDistribution) {
        this.analytics.severityDistribution = this.normalizeSeverity(this.analytics.severityDistribution);
      }
      // trigger charts render if view ready
      setTimeout(() => this.tryRenderCharts(), 100);
    } catch (err) {
      console.error('Analytics fetch failed:', err);
      this.analytics = null;
    } finally {
      this.loading = false;
    }
  }

  // normalize common multilingual keys -> HIGH/MEDIUM/LOW
  private normalizeSeverity(input: Record<string, number>): Record<string, number> {
    const map: Record<string, 'HIGH' | 'MEDIUM' | 'LOW'> = {
      HIGH: 'HIGH', 'उच्च': 'HIGH', 'जास्त': 'HIGH', 'ઊંચું': 'HIGH',
      MEDIUM: 'MEDIUM', 'मध्यम': 'MEDIUM', 'मध्य': 'MEDIUM', 'મધ્યમ': 'MEDIUM',
      LOW: 'LOW', 'निम्न': 'LOW', 'कम': 'LOW', 'નીચું': 'LOW'
    };
    const out: Record<string, number> = { HIGH: 0, MEDIUM: 0, LOW: 0 };
    Object.keys(input).forEach(k => {
      const mapped = map[k.trim()] ?? (['HIGH','MEDIUM','LOW'].includes(k.toUpperCase()) ? k.toUpperCase() as any : null);
      if (mapped) out[mapped] += Number(input[k] ?? 0);
    });
    return out;
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    setTimeout(() => this.tryRenderCharts(), 120);
  }

  private tryRenderCharts() {
    if (!this.viewReady || !this.analytics) return;
    this.renderSymptomChart();
    this.renderSeverityChart();
  }

  private renderSymptomChart() {
    if (!this.symptomChartRef?.nativeElement) return;
    if (this.symptomChart) this.symptomChart.destroy();

    const common = this.analytics?.commonSymptoms ?? {};
    const labels = Object.keys(common);
    const data = Object.values(common).map(v => Number(v));

    if (labels.length === 0) return;

    const ctx = this.symptomChartRef.nativeElement.getContext('2d')!;
    this.symptomChart = new Chart(ctx, {
      type: 'bar',
      data: { labels, datasets: [{ label: 'Frequency', data, backgroundColor: '#3b82f6' }] },
      options: { responsive: true, maintainAspectRatio: false }
    });
  }

  private renderSeverityChart() {
    if (!this.severityChartRef?.nativeElement) return;
    if (this.severityChart) this.severityChart.destroy();

    const dist = this.analytics?.severityDistribution ?? { HIGH: 0, MEDIUM: 0, LOW: 0 };
    const ctx = this.severityChartRef.nativeElement.getContext('2d')!;
    this.severityChart = new Chart(ctx, {
      type: 'pie',
      data: {
        labels: ['HIGH', 'MEDIUM', 'LOW'],
        datasets: [{ data: [dist.HIGH ?? 0, dist.MEDIUM ?? 0, dist.LOW ?? 0], backgroundColor: ['#ef4444', '#f59e0b', '#10b981'] }]
      },
      options: { responsive: true, maintainAspectRatio: false }
    });
  }

  // UI helpers used in template
  getSymptomKeys(): string[] {
    return this.analytics?.commonSymptoms ? Object.keys(this.analytics.commonSymptoms) : [];
  }

  getSeverityKeys(): string[] {
    return this.analytics?.severityDistribution ? Object.keys(this.analytics.severityDistribution) : ['HIGH','MEDIUM','LOW'];
  }

  getSeverityColor(sev: string) {
    switch ((sev||'').toUpperCase()) {
      case 'HIGH': return 'bg-red-600';
      case 'MEDIUM': return 'bg-orange-500';
      case 'LOW': return 'bg-green-600';
      default: return 'bg-gray-500';
    }
  }

  goBack() {
    this.router.navigate(['/admin']);
  }

  ngOnDestroy(): void {
    try { this.symptomChart?.destroy(); } catch {}
    try { this.severityChart?.destroy(); } catch {}
  }
}
