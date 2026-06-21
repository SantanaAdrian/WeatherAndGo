import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit {

  selectedWeather?: WeatherData;

  constructor(
    private weatherService: WeatherService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id') || 'bilbao';
      this.getForecast(id);
    });
  }

  getForecast(id: string): void {
    this.weatherService.getForecastById(id).subscribe({
      next: (result: WeatherData | undefined) => {
        if (!result) {
          this.router.navigate(['error']);
          return;
        }

        this.selectedWeather = result;
      },
      error: () => {
        this.router.navigate(['error']);
      },
      complete: () => {
        console.log('Predicción cargada correctamente');
      }
    });
  }

  getDayName(fecha: string): string {
    const date = new Date(fecha + 'T00:00:00');
    const dayName = date.toLocaleDateString('es-ES', { weekday: 'long' });

    return dayName.charAt(0).toUpperCase() + dayName.slice(1);
  }

  getShortDate(fecha: string): string {
    const date = new Date(fecha + 'T00:00:00');

    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long'
    });
  }
}
