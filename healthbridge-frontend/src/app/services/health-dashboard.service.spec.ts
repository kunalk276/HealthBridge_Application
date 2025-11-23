import { TestBed } from '@angular/core/testing';

import { HealthDashboardService } from './health-dashboard.service';

describe('HealthDashboardService', () => {
  let service: HealthDashboardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HealthDashboardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
