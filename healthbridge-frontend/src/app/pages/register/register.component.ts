import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  constructor(private http: HttpClient,private router: Router) {}

  user = {
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    language: '',
    state: '',
    city: '',
    area: '',
  };

  error = '';
  success = '';
  isPasswordVisible = false;
  isConfirmPasswordVisible = false;

  languages = [
    { code: 'en', label: 'English' },
    { code: 'hi', label: 'हिंदी' },
    { code: 'mr', label: 'मराठी' },
    { code: 'ta', label: 'தமிழ்' },
    { code: 'te', label: 'తెలుగు' },
    { code: 'bn', label: 'বাংলা' },
    { code: 'gu', label: 'ગુજરાતી' },
    { code: 'pa', label: 'ਪੰਜਾਬੀ' },
    { code: 'ml', label: 'മലയാളം' },
    { code: 'kn', label: 'ಕನ್ನಡ' },
  ];

  states = [
    { code: 'MH', name: 'Maharashtra' },
    { code: 'KA', name: 'Karnataka' },
    { code: 'GJ', name: 'Gujarat' },
    { code: 'UP', name: 'Uttar Pradesh' },
    { code: 'DL', name: 'Delhi' },
    { code: 'TN', name: 'Tamil Nadu' },
    { code: 'RJ', name: 'Rajasthan' },
    { code: 'WB', name: 'West Bengal' },
    { code: 'KL', name: 'Kerala' },
    { code: 'PB', name: 'Punjab' },
    { code: 'MP', name: 'Madhya Pradesh' },
  ];

  citiesByState: Record<string, string[]> = {
  MH: [
    'Mumbai','Pune','Nagpur','Nashik','Thane','Aurangabad','Kolhapur',
    'Solapur','Amravati','Navi Mumbai','Jalgaon','Latur','Sangli',
    'Akola','Dhule'
  ],
  KA: [
    'Bengaluru','Mysuru','Mangaluru','Hubli','Belagavi','Kalaburagi','Ballari',
    'Davanagere','Shivamogga','Udupi','Tumakuru','Hassan','Bidar','Vijayapura','Raichur'
  ],
  GJ: [
    'Ahmedabad','Surat','Vadodara','Rajkot','Bhavnagar','Jamnagar','Junagadh',
    'Gandhinagar','Anand','Navsari','Valsad','Morbi','Bharuch','Porbandar','Nadiad'
  ],
  UP: [
    'Lucknow','Kanpur','Varanasi','Prayagraj','Agra','Meerut','Ghaziabad',
    'Mathura','Noida','Bareilly','Moradabad','Aligarh','Jhansi','Gorakhpur','Ayodhya'
  ],
  DL: [
    'New Delhi','Dwarka','Rohini','Saket','Karol Bagh','Chanakyapuri','Lajpat Nagar',
    'Connaught Place','Vasant Kunj','Mayur Vihar','Pitampura','Janakpuri','Shahdara',
    'Rajouri Garden','Greater Kailash'
  ],
  TN: [
    'Chennai','Coimbatore','Madurai','Trichy','Salem','Tirunelveli','Erode',
    'Vellore','Thoothukudi','Kanchipuram','Thanjavur','Nagercoil','Cuddalore',
    'Dindigul','Krishnagiri'
  ],
  RJ: [
    'Jaipur','Jodhpur','Udaipur','Kota','Ajmer','Bikaner','Alwar','Bharatpur',
    'Sri Ganganagar','Pali','Sikar','Tonk','Jaisalmer','Barmer','Chittorgarh'
  ],
  WB: [
    'Kolkata','Howrah','Darjeeling','Durgapur','Asansol','Siliguri','Haldia',
    'Kharagpur','Bardhaman','Malda','Jalpaiguri','Balurghat','Krishnanagar',
    'Cooch Behar','Bankura'
  ],
  KL: [
    'Kochi','Thiruvananthapuram','Kozhikode','Thrissur','Kannur','Kollam','Palakkad',
    'Alappuzha','Kottayam','Malappuram','Pathanamthitta','Kasargod','Manjeri',
    'Nedumangad','Thalassery'
  ],
  PB: [
    'Ludhiana','Amritsar','Jalandhar','Patiala','Bathinda','Mohali','Hoshiarpur',
    'Pathankot','Firozpur','Moga','Barnala','Kapurthala','Muktsar','Sangrur','Rajpura'
  ],
  MP: [
    'Bhopal','Indore','Gwalior','Jabalpur','Ujjain','Sagar','Rewa','Satna','Ratlam',
    'Chhindwara','Khandwa','Dewas','Shivpuri','Vidisha','Neemuch'
  ],
};
private levenshtein(a: string, b: string): number {
  const matrix = Array.from({ length: a.length + 1 }, (_, i) => [i]);

  for (let j = 1; j <= b.length; j++) matrix[0][j] = j;

  for (let i = 1; i <= a.length; i++) {
    for (let j = 1; j <= b.length; j++) {
      matrix[i][j] = Math.min(
        matrix[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1),
        matrix[i][j - 1] + 1,
        matrix[i - 1][j] + 1
      );
    }
  }
  return matrix[a.length][b.length];
}

getCitySuggestions(input: string): string[] {
  const query = input.toLowerCase();
  const allCities = Object.values(this.citiesByState).flat();

  return allCities
    .map(city => ({
      city,
      score: this.levenshtein(query, city.toLowerCase()),
    }))
    .sort((a, b) => a.score - b.score)
    .slice(0, 10)
    .map(x => x.city);
}

  onStateChange() {
    this.user.city = '';
  }

  get cities(): string[] {
    return this.citiesByState[this.user.state] || [];
  }

  togglePasswordVisibility() {
    this.isPasswordVisible = !this.isPasswordVisible;
  }

  toggleConfirmPasswordVisibility() {
    this.isConfirmPasswordVisible = !this.isConfirmPasswordVisible;
  }

  onPhoneInput(event: KeyboardEvent) {
    const char = event.key;
    if (!/[0-9]/.test(char)) {
      event.preventDefault();
    }
  }

  validate(): boolean {
    const { username, email, phone, password, confirmPassword, language, state, city, area } = this.user;

    if (!username.trim()) return this.setError('Username is required.');
    if (/\s/.test(username)) return this.setError('Username cannot contain spaces.');
   // if (!email.trim()) return this.setError('Email is required.');
    //if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return this.setError('Enter a valid email address.');
    if (!/^[0-9]{10}$/.test(phone)) return this.setError('Phone number must be 10 digits.');
    if (!password.trim()) return this.setError('Password is required.');

    const strongPassword = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;
    if (!strongPassword.test(password))
      return this.setError('Password must include uppercase letter, number, and special character.');

    if (password !== confirmPassword) return this.setError('Passwords do not match.');
    if (!language) return this.setError('Please select a language.');
    if (!state) return this.setError('Please select your state.');
    if (!city) return this.setError('Please select your city.');
    if (!area.trim()) return this.setError('Please enter your area.');

    this.error = '';
    return true;
  }

  setError(msg: string): boolean {
    this.error = msg;
    return false;
  }

// onSubmit(form: NgForm) {
//   if (!this.validate()) return;

//   const { confirmPassword, ...payload } = this.user;
  
//   console.log('🚀 About to send request...');
//   console.log('📦 Payload:', payload);

//   this.http.post('http://localhost:8080/api/auth/register', payload, {
//     headers: new HttpHeaders({
//       'Content-Type': 'application/json',
//     }),
//   })
//   .subscribe({
//     next: (res: any) => {
//       console.log('✅ Success:', res);
//       this.success = `Account created successfully for ${this.user.username}`;
//       this.error = '';
//       alert(this.success);
//       form.resetForm();
//     },
//     error: (err) => {
//       console.error('❌ Error Status:', err.status);
//       console.error('❌ Error Details:', err);
      
//       if (err.status === 401) {
//         this.error = 'Authentication required. Please check backend security configuration.';
//       } else if (err.status === 400) {
//         this.error = err.error?.message || 'Invalid input. Please check the form fields.';
//       } else if (err.status === 500) {
//         this.error = 'Server error occurred. Please try again later.';
//       } else if (err.status === 0) {
//         this.error = 'Cannot connect to server. Please check if the backend is running.';
//       } else {
//         this.error = err.error?.message || 'Registration failed. Please try again.';
//       }
//       this.success = '';
//     },
//   });
// }
onSubmit(form: NgForm) {
  if (!this.validate()) return;

  const { confirmPassword, ...payload } = this.user;

  console.log('📤 Sending registration:', payload);

  this.http.post('http://localhost:8080/api/auth/register', payload, {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
    }),
  })
  .subscribe({
    next: (res: any) => {
      console.log('✅ Registration successful:', res);
      this.success = `Account created successfully for ${this.user.username}! Redirecting to login...`;
      this.error = '';
      form.resetForm();
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 3000);
    },
    error: (err) => {
      console.error('❌ Registration error:', err);

      if (err.status === 400 && err.error?.message) {
        this.error = err.error.message;
      } else if (err.status === 400) {
        this.error = 'Invalid input. Please check all fields.';
      } else if (err.status === 500) {
        this.error = 'Server error. Please try again later.';
      } else if (err.status === 0) {
        this.error = 'Cannot connect to server. Is Spring Boot running on port 8080?';
      } else {
        this.error = err.error?.message || 'Registration failed. Please try again.';
      }
      this.success = '';
    },
  });
}
}