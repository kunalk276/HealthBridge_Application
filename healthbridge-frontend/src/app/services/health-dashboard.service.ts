import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class HealthDashboardService {
  private smsUrl = 'http://localhost:8080/api/sms/send';
  //private aiUrl = 'http://localhost:8080/api/symptoms/analyze';
   private aiUrl = 'http://localhost:8080/api/symptoms/analyze';
private analyticsUrl = 'http://localhost:8080/api/analytics';


  //private analyticsUrl = 'http://localhost:8080/api/analytics/user';
  private translateUrl = 'http://localhost:8080/api/translate';
  private suggestionsApi = 'http://localhost:8080/api/symptoms/suggestions';

  constructor(private http: HttpClient) {}

  /* -----------------------------------------------------
     USER ANALYTICS
  ----------------------------------------------------- */
// src/app/services/health-dashboard.service.ts
async getUserAnalytics(userId: number): Promise<any> {
  try {
    const url = `${this.analyticsUrl}/user/${userId}`;

    // build Authorization header from stored credentials
    const username = localStorage.getItem('username') || '';
    const password = localStorage.getItem('password') || '';
    const authHeader = { Authorization: 'Basic ' + btoa(`${username}:${password}`) };

    return await this.http.get(url, {
      headers: authHeader,
      withCredentials: true
    }).toPromise();
  } catch (error) {
    console.error('Failed to fetch user analytics:', error);
    return null;
  }
}


  /* -----------------------------------------------------
     LOCATION
  ----------------------------------------------------- */
  async detectUserLocation(): Promise<{ lat: number; lon: number }> {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        pos => resolve({ lat: pos.coords.latitude, lon: pos.coords.longitude }),
        err => reject(err),
        { enableHighAccuracy: true }
      );
    });
  }

  async reverseGeocode(lat: number, lon: number): Promise<string> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`;
    try {
      const data: any = await this.http.get(url).toPromise();
      return data?.display_name || 'Unknown address';
    } catch {
      return 'Address not found';
    }
  }

  /* -----------------------------------------------------
     HOSPITALS
  ----------------------------------------------------- */
  async fetchNearbyHospitals(lat: number, lon: number, radius = 5000): Promise<any[]> {
    const query = `
      [out:json];
      (
        node["amenity"="hospital"](around:${radius},${lat},${lon});
        way["amenity"="hospital"](around:${radius},${lat},${lon});
        relation["amenity"="hospital"](around:${radius},${lat},${lon});
      );
      out center;
    `;
    const url = `https://overpass-api.de/api/interpreter?data=${encodeURIComponent(query)}`;

    try {
      const res: any = await this.http.get(url).toPromise();

      return (res?.elements || []).map((el: any) => {
        const lat2 = el.lat ?? el.center?.lat;
        const lon2 = el.lon ?? el.center?.lon;

        return {
          name: el.tags?.name || 'Unnamed Hospital',
          address:
            el.tags?.['addr:full'] ||
            `${el.tags?.['addr:street'] || ''} ${el.tags?.['addr:city'] || ''}`.trim(),
          lat: lat2,
          lon: lon2,
          contact:
            el.tags?.phone ||
            el.tags?.telephone ||
            el.tags?.['contact:phone'] ||
            el.tags?.['contact:mobile'] ||
            'Not available',
          distance: this.calculateDistance(lat, lon, lat2, lon2),
        };
      });
    } catch {
      return [];
    }
  }

  /* -----------------------------------------------------
     SEND SYMPTOMS TO AI BACKEND
  ----------------------------------------------------- */
  async sendSymptomsToBackend(body: any): Promise<any> {
    return await this.http.post(this.aiUrl, body).toPromise();
  }

  /* -----------------------------------------------------
     AUTO-TRANSLATE RESPONSE
  ----------------------------------------------------- */
  async translateText(text: string, targetLang: string): Promise<string> {
    try {
      const body = { text, targetLang };
      const result: any = await this.http.post(this.translateUrl, body).toPromise();
      return result?.translatedText || text;
    } catch {
      return text;
    }
  }

  /* -----------------------------------------------------
     SYMPTOM SUGGESTIONS
  ----------------------------------------------------- */
  async getSymptomSuggestions(lang: string): Promise<string[]> {
    try {
      const url = `${this.suggestionsApi}?lang=${lang}`;
      const result: any = await this.http.get(url).toPromise();
      return result?.suggestions || [];
    } catch {
      return [];
    }
  }

  // fallback to local JSON under assets/i18n/symptoms
  async getLocalSymptomSuggestions(lang: string): Promise<string[]> {
    try {
      const url = `assets/symptoms/symptoms-${lang}.json`;
      const data: any = await this.http.get(url).toPromise();
      return data?.suggestions || [];
    } catch {
      return [];
    }
  }

  /* -----------------------------------------------------
     SMS ALERT (no changes required)
  ----------------------------------------------------- */
 async sendSmsAlert(payload: {
  phone: string;
  userName: string;
  severity: string;
  hospitalName: string;
  hospitalContact: string;
  googleMapLink: string;
}): Promise<any> {
  try {
    return await this.http.post(this.smsUrl, payload).toPromise();
  } catch (err) {
    console.error('SMS sending failed:', err);
    return null;
  }
}
  /* -----------------------------------------------------
     EMERGENCY FLOW (unchanged)
  ----------------------------------------------------- */
  /* -----------------------------------------------------
   EMERGENCY FLOW (UPDATED)
----------------------------------------------------- */
async sendEmergencyAlert(
  userPhone: string,
  severity: string,
  severityScore: number,
  userName: string
) {
  if (severity !== 'HIGH') return;

  try {
    const { lat, lon } = await this.detectUserLocation();
    const hospitals = await this.fetchNearbyHospitals(lat, lon);

    if (!hospitals.length) return;

    const nearest = hospitals[0];

    const payload = {
      phone: userPhone.startsWith('+') ? userPhone : '+91' + userPhone,
      userName: userName,
      severity: severity, // FIXED
      hospitalName: nearest.name,
      hospitalContact: nearest.contact ?? 'Not available',
      googleMapLink: `https://maps.google.com/maps?q=${nearest.lat},${nearest.lon}` // FIXED
    };

    await this.sendSmsAlert(payload);
  } catch (error) {
    console.error('Emergency alert failed', error);
  }
}


  /* -----------------------------------------------------
     MATH HELPERS
  ----------------------------------------------------- */
  private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): string {
    const R = 6371;
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(this.toRad(lat1)) *
      Math.cos(this.toRad(lat2)) *
      Math.sin(dLon / 2) ** 2;

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return (R * c).toFixed(2);
  }

  private toRad(v: number) {
    return (v * Math.PI) / 180;
  }


  
}
