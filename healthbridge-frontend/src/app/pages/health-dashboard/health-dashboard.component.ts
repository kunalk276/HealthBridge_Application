import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HealthDashboardService } from '../../services/health-dashboard.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';

interface Hospital {
  name: string;
  address: string;
  lat: number;
  lon: number;
  distance?: string;
  contact?: string;
}

interface UserAnalytics {
  totalRecords: number;
  commonSymptoms: { [key: string]: number };
  severityDistribution: { [key: string]: number };
  healthStatus: string;
}

@Component({
  selector: 'app-health-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './health-dashboard.component.html',
  styleUrls: ['./health-dashboard.component.css']
})
export class HealthDashboardComponent implements OnInit, OnDestroy {

  userName = '';
  userId: number | null = null;
  userLocation = 'Fetching location...';
  hospitals: Hospital[] = [];

  message = '';
  aiResponse: string | null = null;
  isLoadingAI = false;

  isListening = false;
  recognition: any = null;
  audioContext: any;
  analyser: any;
  animationFrame: any;
  waveformData: Uint8Array | null = null;

  suggestedSymptoms: string[] = [];

  userAnalytics: UserAnalytics | null = null;
  isLoadingAnalytics = false;
  showAnalytics = false;

  smsSending = false;
  smsSent = false;
  smsError: string | null = null;

  severityScore = 0;
  severityLevel = '';

  languages = [
    { label: 'English', code: 'en' },
    { label: 'Hindi', code: 'hi' },
    { label: 'Marathi', code: 'mr' },
    { label: 'Tamil', code: 'ta' },
    { label: 'Telugu', code: 'te' },
    { label: 'Gujarati', code: 'gu' },
    { label: 'Kannada', code: 'kn' }
  ];
  selectedLanguage = 'en';

  private sub: Subscription | null = null;

  constructor(
    private healthService: HealthDashboardService,
    private authService: AuthService,
    private translate: TranslateService
  ) {}

  async ngOnInit(): Promise<void> {
  this.userName = this.authService.getLoggedInUser() || 'Guest';
  this.userId = this.authService.getUserId();

  const savedLang = this.authService.getUserLanguage();
  const browserLang = this.translate.getBrowserLang?.();
  const finalLang =
    savedLang && this.translate.getLangs().includes(savedLang)
      ? savedLang
      : browserLang && this.translate.getLangs().includes(browserLang)
      ? browserLang
      : 'en';

  this.setAppLanguage(finalLang);

  try {
    const pos = await this.healthService.detectUserLocation();
    this.userLocation = await this.healthService.reverseGeocode(pos.lat, pos.lon);
    const list = await this.healthService.fetchNearbyHospitals(pos.lat, pos.lon);
    this.hospitals = this.sortHospitalsByContact(list);
  } catch (err) {
    console.warn('Location/hospitals load failed, using default:', err);
    this.userLocation = this.translate.instant('DASHBOARD.LOC_UNAVAILABLE');
    const list = await this.healthService.fetchNearbyHospitals(18.5204, 73.8567);
    this.hospitals = this.sortHospitalsByContact(list);
  }

  if (this.userId) {
    this.loadUserAnalytics();
  }
  this.loadSymptomSuggestions();
}


  ngOnDestroy(): void {
    if (this.sub) this.sub.unsubscribe();
    try { cancelAnimationFrame(this.animationFrame); } catch {}
    if (this.recognition) this.recognition.stop?.();
  }

  // set app language and persist
  setAppLanguage(lang: string) {
    if (!lang) return;
    this.selectedLanguage = lang;
    try {
      this.translate.use(lang);
    } catch (err) {
      console.warn('translate.use failed:', err);
    }
    localStorage.setItem('userLanguage', lang);

    if (this.recognition) {
      this.recognition.lang = this.mapSpeechLang(lang);
    }

    // reload suggestions / other localized resources
    this.loadSymptomSuggestions();
  }

  private mapSpeechLang(langCode: string) {
    const map: any = {
      en: 'en-IN',
      hi: 'hi-IN',
      mr: 'mr-IN',
      ta: 'ta-IN',
      te: 'te-IN',
      gu: 'gu-IN',
      kn: 'kn-IN'
    };
    return map[langCode] || 'en-IN';
  }

  private sortHospitalsByContact(list: Hospital[]) {
    return list.sort((a, b) => {
      const hasContactA = !!a.contact && a.contact !== 'Not available';
      const hasContactB = !!b.contact && b.contact !== 'Not available';
      return hasContactA === hasContactB ? 0 : hasContactA ? -1 : 1;
    });
  }

  // ---------------------------
  // Analytics
  // ---------------------------
  async loadUserAnalytics() {
    if (!this.userId) return;
    this.isLoadingAnalytics = true;
    try {
      const result = await this.healthService.getUserAnalytics(this.userId!);
      if (result?.success && result?.data) {
        this.userAnalytics = result.data;
      } else {
        // if backend returns raw object directly
        if (result && typeof result.totalRecords === 'number') {
          this.userAnalytics = result as UserAnalytics;
        } else {
          console.warn('No analytics data or unexpected shape', result);
        }
      }
    } catch (err) {
      console.error('Error loading analytics:', err);
    } finally {
      this.isLoadingAnalytics = false;
    }
  }

  toggleAnalytics() {
    this.showAnalytics = !this.showAnalytics;
    if (this.showAnalytics && !this.userAnalytics) {
      this.loadUserAnalytics();
    }
  }

  getSymptomKeys(): string[] {
    return this.userAnalytics?.commonSymptoms ? Object.keys(this.userAnalytics.commonSymptoms) : [];
  }

  getSeverityKeys(): string[] {
    return this.userAnalytics?.severityDistribution ? Object.keys(this.userAnalytics.severityDistribution) : [];
  }

  getSeverityColor(severity: string): string {
    switch ((severity || '').toUpperCase()) {
      case 'HIGH': return 'bg-red-600';
      case 'MEDIUM': return 'bg-orange-500';
      case 'LOW': return 'bg-green-600';
      default: return 'bg-gray-500';
    }
  }

  getHealthStatusColor(): string {
    if (!this.userAnalytics?.healthStatus) return 'text-gray-700';
    const status = this.userAnalytics.healthStatus.toLowerCase();
    if (status.includes('good')) return 'text-green-600';
    if (status.includes('moderate')) return 'text-orange-600';
    if (status.includes('concerning')) return 'text-red-600';
    return 'text-gray-700';
  }

  // ---------------------------
  // Symptom suggestions
  // ---------------------------
  async loadSymptomSuggestions() {
    try {
      const backend = await this.healthService.getSymptomSuggestions(this.selectedLanguage);
      if (backend?.length) {
        this.suggestedSymptoms = backend;
        return;
      }
    } catch (err) {
      // ignore and fallback
    }

    try {
      const local = await this.healthService.getLocalSymptomSuggestions(this.selectedLanguage);
      this.suggestedSymptoms = local || [];
    } catch (err) {
      console.warn('No local symptom suggestions:', err);
      this.suggestedSymptoms = [];
    }
  }

  pickSuggestion(s: string) {
    this.message = s;
  }

  // ---------------------------
  // Send message to backend
  // ---------------------------
  async sendMessage() {
    if (!this.message?.trim()) return;

    this.isLoadingAI = true;
    this.aiResponse = null;
    this.smsSent = false;
    this.smsError = null;

    try {
      const pos = await this.healthService.detectUserLocation();
      const payload = {
        symptoms: this.message,
        language: this.selectedLanguage,
        latitude: pos.lat,
        longitude: pos.lon
      };

      const result: any = await this.healthService.sendSymptomsToBackend(payload);
      this.isLoadingAI = false;

      if (result?.success && result?.data) {
        const backendText = result.data.aiResponse ?? result.data.text ?? '';
        const responseLang = result.data.language;

        if (responseLang === this.selectedLanguage || (!responseLang && this.selectedLanguage === 'en')) {
          this.aiResponse = backendText;
        } else {
          try {
            this.aiResponse = await this.healthService.translateText(backendText, this.selectedLanguage);
          } catch (err) {
            console.warn('Translate failed, fallback to original text', err);
            this.aiResponse = backendText;
          }
        }

        this.severityLevel = result.data.severityLevel;
        this.severityScore = result.data.severityScore;

        // refresh analytics
        this.loadUserAnalytics();

        // auto SMS if serious
       if ((this.severityLevel === 'HIGH' || this.severityLevel === 'EMERGENCY') && !this.smsSent) {

   await this.sendSmsAlertToUser();
}
console.log("Severity Level:", this.severityLevel);
console.log("Severity Score:", this.severityScore);

      } else {
        this.aiResponse = this.translate.instant('DASHBOARD.ERROR_AI') || 'Unable to analyze symptoms.';
      }
    } catch (err) {
      this.isLoadingAI = false;
      console.error('sendMessage error:', err);
      this.aiResponse = this.translate.instant('DASHBOARD.SERVER_ERROR') || 'Server error.';
    }
  }

  // ---------------------------
  // Voice recognition
  // ---------------------------
  toggleVoiceInput() {
    const SpeechRecognition = (window as any).webkitSpeechRecognition || (window as any).SpeechRecognition;
    if (!SpeechRecognition) {
      alert(this.translate.instant('DASHBOARD.VOICE_NOT_SUPPORTED') || 'Voice not supported');
      return;
    }

    if (!this.recognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.lang = this.mapSpeechLang(this.selectedLanguage);
      this.recognition.continuous = true;
      this.recognition.interimResults = true;

      this.recognition.onresult = (event: any) => {
        let finalText = '';
        let interimText = '';
        for (let i = 0; i < event.results.length; i++) {
          if (event.results[i].isFinal) {
            finalText += event.results[i][0].transcript;
          } else {
            interimText += event.results[i][0].transcript;
          }
        }
        this.message = (finalText + interimText).trim();
      };

      this.recognition.onerror = () => this.stopVoice();
      this.recognition.onend = () => this.stopVoice();
    }

    if (this.isListening) {
      this.stopVoice();
      return;
    }

    this.startVoice();
  }

  startVoice() {
    this.isListening = true;
    this.message = '';
    try {
      this.recognition.lang = this.mapSpeechLang(this.selectedLanguage);
      this.recognition.start();
      this.startWaveform();
    } catch (err) {
      console.warn('startVoice failed', err);
      this.isListening = false;
    }
  }

  stopVoice() {
    this.isListening = false;
    if (this.recognition) {
      try { this.recognition.stop(); } catch {}
    }
    try { cancelAnimationFrame(this.animationFrame); } catch {}
    this.waveformData = null;
  }

  startWaveform() {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then((stream) => {
        this.audioContext = new AudioContext();
        const source = this.audioContext.createMediaStreamSource(stream);
        this.analyser = this.audioContext.createAnalyser();
        this.analyser.fftSize = 256;
        source.connect(this.analyser);
        this.waveformData = new Uint8Array(this.analyser.frequencyBinCount);

        const animate = () => {
          this.analyser.getByteFrequencyData(this.waveformData!);
          this.animationFrame = requestAnimationFrame(animate);
        };
        animate();
      })
      .catch((err) => {
        console.warn('Microphone access denied', err);
      });
  }

  // ---------------------------
  // Map
  // ---------------------------
  openMap(hospital: Hospital) {
    window.open(`https://maps.google.com/maps?q=${hospital.lat},${hospital.lon}`, '_blank');
  }

  // ---------------------------
  // SMS Alert
  // ---------------------------
 async sendSmsAlertToUser() {
  const userPhone = this.authService.getUserPhone();
  if (!userPhone) {
    this.smsError = 'No phone number on file.';
    return;
  }

  if (!this.hospitals.length) {
    this.smsError = 'No nearby hospital data available.';
    return;
  }

  this.smsSending = true;
  this.smsError = null;

  const nearest = this.hospitals[0];

  // FIXED PAYLOAD
  const smsPayload = {
    phone: userPhone.startsWith('+') ? userPhone : '+91' + userPhone,
    userName: this.userName || 'User',
    severity: this.severityLevel,
    hospitalName: nearest.name,
    hospitalContact: nearest.contact ?? 'Not available',
    googleMapLink: `https://maps.google.com/maps?q=${nearest.lat},${nearest.lon}`
  };

  try {
  const result: any = await this.healthService.sendSmsAlert(smsPayload);

  // Always show success popup if no exception was thrown
  this.smsSent = true;
  alert("Message sent successfully!");
} catch (err) {
  console.error('sendSmsAlert exception:', err);
  this.smsError = 'Error sending SMS.';
} finally {
  this.smsSending = false;
}
 }
}
