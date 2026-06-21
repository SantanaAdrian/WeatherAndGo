import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

interface CityOption {
  id: string;
  name: string;
}

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {

  selectedCityId: string = 'ubicacion-actual';
  selectedWeather?: WeatherData;
  loading: boolean = false;
  errorMessage: string = '';

  cityOptions: CityOption[] = [
  { id: 'ubicacion-actual', name: 'Ubicación actual' },
  { id: 'bilbao', name: 'Bilbao' },
  { id: 'madrid', name: 'Madrid' },
  { id: 'barcelona', name: 'Barcelona' },
  { id: 'valencia', name: 'Valencia' },
  { id: 'sevilla', name: 'Sevilla' },
  { id: 'santander', name: 'Santander' }
  ];

  constructor(
    private weatherService: WeatherService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id') || 'ubicacion-actual';
      this.selectedCityId = id;
      this.loadRecommendation(id);
    });
  }

  loadRecommendation(cityId: string): void {
    this.selectedCityId = cityId;
    this.loading = true;
    this.errorMessage = '';

    this.weatherService.getForecastById(cityId).subscribe({
      next: (result: WeatherData | undefined) => {
        if (!result) {
          this.errorMessage = 'No se ha podido cargar la recomendación inteligente.';
          this.selectedWeather = undefined;
          this.loading = false;
          return;
        }

        this.selectedWeather = result;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error al obtener la recomendación inteligente.';
        this.selectedWeather = undefined;
        this.loading = false;
      }
    });
  }

  onCityChange(): void {
    this.router.navigate(['/recommendations', this.selectedCityId]);
  }
}
