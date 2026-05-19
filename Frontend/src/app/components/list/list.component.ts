import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

interface HourlyForecast {
  hora: string;
  temperatura: number;
  estadoCielo: string;
  probabilidadLluvia: number;
  viento: number;
}

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit {

  selectedWeather?: WeatherData;
  hourlyForecast: HourlyForecast[] = [];

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
        this.hourlyForecast = this.buildHourlyForecast(result);
      },
      error: () => {
        this.router.navigate(['error']);
      },
      complete: () => {
        console.log('Predicción cargada correctamente');
      }
    });
  }

  buildHourlyForecast(weather: WeatherData): HourlyForecast[] {
    return [
      {
        hora: '08:00',
        temperatura: weather.temperatura - 3,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: weather.probabilidadLluvia,
        viento: weather.viento
      },
      {
        hora: '11:00',
        temperatura: weather.temperatura - 1,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: weather.probabilidadLluvia,
        viento: weather.viento + 1
      },
      {
        hora: '14:00',
        temperatura: weather.temperatura + 2,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: Math.max(weather.probabilidadLluvia - 10, 0),
        viento: weather.viento + 2
      },
      {
        hora: '17:00',
        temperatura: weather.temperatura + 1,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: weather.probabilidadLluvia,
        viento: weather.viento + 1
      },
      {
        hora: '20:00',
        temperatura: weather.temperatura - 1,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: weather.probabilidadLluvia + 5,
        viento: weather.viento
      },
      {
        hora: '23:00',
        temperatura: weather.temperatura - 3,
        estadoCielo: weather.estadoCielo,
        probabilidadLluvia: weather.probabilidadLluvia + 5,
        viento: Math.max(weather.viento - 2, 0)
      }
    ];
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
