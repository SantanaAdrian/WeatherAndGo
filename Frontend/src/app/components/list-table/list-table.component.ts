import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WeatherData, WeatherProviderData, WeatherService } from 'src/app/services/weather.service';

interface CityOption {
  id: string;
  name: string;
}

interface ProviderComparisonRow {
  provider: string;
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitationProbability: number;
  weatherStatus: string;
  isAggregated: boolean;
}

@Component({
  selector: 'app-list-table',
  templateUrl: './list-table.component.html',
  styleUrls: ['./list-table.component.css']
})
export class ListTableComponent implements OnInit {
  selectedCityId: string = 'ubicacion-actual';
  selectedWeather?: WeatherData;
  comparisonRows: ProviderComparisonRow[] = [];
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
      const id = params.get('id') || this.weatherService.getActiveLocationId();
      this.loadComparison(id);
    });
  }

  loadComparison(cityId: string): void {
    this.selectedCityId = cityId;
    this.loading = true;
    this.errorMessage = '';
    this.weatherService.setActiveLocationId(cityId);

    this.weatherService.getForecastById(cityId).subscribe({
      next: (result: WeatherData | undefined) => {
        if (!result) {
          this.errorMessage = 'No se han podido cargar los datos meteorológicos.';
          this.selectedWeather = undefined;
          this.comparisonRows = [];
          this.loading = false;
          return;
        }

        this.selectedWeather = result;
        this.ensureCityOption(result);
        this.comparisonRows = this.buildComparisonRows(result);
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error al obtener la comparativa de proveedores.';
        this.selectedWeather = undefined;
        this.comparisonRows = [];
        this.loading = false;
      }
    });
  }

  onCityChange(): void {
    this.router.navigate(['/list-table', this.selectedCityId]);
  }

  private ensureCityOption(weather: WeatherData): void {
    const exists = this.cityOptions.some(option => option.id === weather.id);

    if (!exists) {
      this.cityOptions = [
        { id: weather.id, name: weather.ciudad },
        ...this.cityOptions
      ];
    }
  }

  private buildComparisonRows(weather: WeatherData): ProviderComparisonRow[] {
    const rows: ProviderComparisonRow[] = [];

    rows.push({
      provider: 'WEATHER&GO',
      temperature: weather.temperatura,
      humidity: weather.humedad,
      windSpeed: weather.viento,
      precipitationProbability: weather.probabilidadLluvia,
      weatherStatus: weather.estadoCielo,
      isAggregated: true
    });

    (weather.providerData || []).forEach((provider: WeatherProviderData) => {
      rows.push({
        provider: provider.provider,
        temperature: provider.temperature,
        humidity: provider.humidity,
        windSpeed: provider.windSpeed,
        precipitationProbability: provider.precipitationProbability,
        weatherStatus: provider.weatherStatus,
        isAggregated: false
      });
    });

    return rows;
  }
}
