import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface ForecastDay {
  fecha: string;
  temperaturaMaxima: number;
  temperaturaMinima: number;
  humedad: number;
  viento: number;
  probabilidadLluvia: number;
  estadoCielo: string;
  recomendacion: string;
}

export interface HourlyForecast {
  hora: string;
  temperatura: number;
  estadoCielo: string;
  probabilidadLluvia: number;
  viento: number;
}

export interface WeatherProviderData {
  provider: string;
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitationProbability: number;
  weatherStatus: string;
}

export interface PlanRecommendation {
  category: string;
  confidence: number;
  summary: string;
  recommendedPlanTypes: string[];
  reasons: string[];
}

export interface WeatherData {
  id: string;
  ciudad: string;
  latitud: number;
  longitud: number;
  fecha: string;
  temperatura: number;
  humedad: number;
  viento: number;
  probabilidadLluvia: number;
  estadoCielo: string;
  descripcion: string;
  recomendacion: string;
  categoriaRecomendada: string;
  forecast: ForecastDay[];
  hourlyForecast: HourlyForecast[];
  providerData: WeatherProviderData[];
  planRecommendation: PlanRecommendation;
}

export interface WeatherQueryLog {
  id: number;
  latitude: number;
  longitude: number;
  locationName: string;
  currentTemperature: number;
  currentHumidity: number;
  currentWindSpeed: number;
  currentPrecipitationProbability: number;
  currentWeatherStatus: string;
  providers: string;
  createdAt: string;
}

interface BackendHourlyForecast {
  time: string;
  temperature: number;
  precipitationProbability: number;
  windSpeed: number;
  weatherStatus: string;
}

interface BackendDailyForecast {
  date: string;
  dayName: string;
  maxTemperature: number;
  minTemperature: number;
  precipitationProbability: number;
  windSpeed: number;
  weatherStatus: string;
  recommendation: string;
}

interface BackendWeatherSource {
  provider: string;
  status: string;
}

interface BackendWeatherProviderData {
  provider: string;
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitationProbability: number;
  weatherStatus: string;
}

interface BackendPlanRecommendation {
  category: string;
  confidence: number;
  summary: string;
  recommendedPlanTypes: string[];
  reasons: string[];
}

interface BackendWeatherForecastResponse {
  locationName: string;
  latitude: number;
  longitude: number;
  timezone: string;
  currentTemperature: number;
  currentHumidity: number;
  currentWindSpeed: number;
  currentPrecipitationProbability: number;
  currentWeatherStatus: string;
  currentRecommendation: string;
  hourlyForecast: BackendHourlyForecast[];
  dailyForecast: BackendDailyForecast[];
  sources: BackendWeatherSource[];
  providerData: BackendWeatherProviderData[];
  planRecommendation?: BackendPlanRecommendation;
}

interface CityLocation {
  id: string;
  ciudad: string;
  lat: number;
  lon: number;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {

  private readonly backendUrl = 'http://localhost:8080/api/weather/forecast';
  private readonly logsUrl = 'http://localhost:8080/api/weather/logs';

  private readonly cityLocations: CityLocation[] = [
    {
      id: 'bilbao',
      ciudad: 'Bilbao',
      lat: 43.263,
      lon: -2.935
    },
    {
      id: 'madrid',
      ciudad: 'Madrid',
      lat: 40.4168,
      lon: -3.7038
    },
    {
      id: 'barcelona',
      ciudad: 'Barcelona',
      lat: 41.3874,
      lon: 2.1686
    },
    {
      id: 'valencia',
      ciudad: 'Valencia',
      lat: 39.4699,
      lon: -0.3763
    },
    {
      id: 'sevilla',
      ciudad: 'Sevilla',
      lat: 37.3891,
      lon: -5.9845
    },
    {
      id: 'santander',
      ciudad: 'Santander',
      lat: 43.4623,
      lon: -3.8099
    }
  ];

  constructor(private http: HttpClient) { }

  getWeatherList(): Observable<WeatherData[]> {
    const requests = this.cityLocations.map(city =>
      this.getWeatherByCoordinates(city.lat, city.lon, city.id, city.ciudad).pipe(
        catchError(error => {
          console.error(`Error cargando datos de ${city.ciudad}`, error);
          return of(null);
        })
      )
    );

    return forkJoin(requests).pipe(
      map(results => results.filter((item): item is WeatherData => item !== null))
    );
  }

  getWeatherById(id: string): Observable<WeatherData | undefined> {
    return this.getForecastById(id);
  }

  getForecastById(id: string): Observable<WeatherData | undefined> {
    const city = this.cityLocations.find(item => item.id === id);

    if (!city) {
      return of(undefined);
    }

    return this.getWeatherByCoordinates(city.lat, city.lon, city.id, city.ciudad);
  }

  getWeatherByCoordinates(
    lat: number,
    lon: number,
    id: string = 'ubicacion-actual',
    ciudad: string = 'Tu ubicación actual'
  ): Observable<WeatherData> {
    const url = `${this.backendUrl}?lat=${lat}&lon=${lon}`;

    return this.http.get<BackendWeatherForecastResponse>(url).pipe(
      map(response => this.mapBackendResponseToWeatherData(response, id, ciudad))
    );
  }

  getWeatherLogs(): Observable<WeatherQueryLog[]> {
    return this.http.get<WeatherQueryLog[]>(this.logsUrl);
  }

  getLatestWeatherLogs(): Observable<WeatherQueryLog[]> {
    return this.http.get<WeatherQueryLog[]>(`${this.logsUrl}/latest`);
  }

  getWeatherLogsCount(): Observable<number> {
    return this.http.get<number>(`${this.logsUrl}/count`);
  }

  deleteWeatherLogs(): Observable<void> {
    return this.http.delete<void>(this.logsUrl);
  }

  private mapBackendResponseToWeatherData(
    response: BackendWeatherForecastResponse,
    id: string,
    ciudad: string
  ): WeatherData {
    const fechaActual = response.dailyForecast && response.dailyForecast.length > 0
      ? response.dailyForecast[0].date
      : new Date().toISOString().split('T')[0];

    const resolvedCity = ciudad === 'Tu ubicación actual' && response.locationName
      ? response.locationName
      : ciudad;

    return {
      id: id,
      ciudad: resolvedCity,
      latitud: response.latitude,
      longitud: response.longitude,
      fecha: fechaActual,
      temperatura: Math.round(response.currentTemperature),
      humedad: response.currentHumidity,
      viento: Math.round(response.currentWindSpeed),
      probabilidadLluvia: response.currentPrecipitationProbability,
      estadoCielo: response.currentWeatherStatus,
      descripcion: this.buildDescription(response),
      recomendacion: response.currentRecommendation,
      categoriaRecomendada: this.getRecommendationCategory(response),
      forecast: this.mapDailyForecast(response),
      hourlyForecast: this.mapHourlyForecast(response),
      providerData: this.mapProviderData(response),
      planRecommendation: this.mapPlanRecommendation(response)
    };
  }

  private mapDailyForecast(response: BackendWeatherForecastResponse): ForecastDay[] {
    if (!response.dailyForecast) {
      return [];
    }

    return response.dailyForecast.map(day => ({
      fecha: day.date,
      temperaturaMaxima: Math.round(day.maxTemperature),
      temperaturaMinima: Math.round(day.minTemperature),
      humedad: response.currentHumidity,
      viento: Math.round(day.windSpeed),
      probabilidadLluvia: day.precipitationProbability,
      estadoCielo: day.weatherStatus,
      recomendacion: day.recommendation
    }));
  }

  private mapHourlyForecast(response: BackendWeatherForecastResponse): HourlyForecast[] {
    if (!response.hourlyForecast) {
      return [];
    }

    return response.hourlyForecast.map(hour => ({
      hora: this.formatHour(hour.time),
      temperatura: Math.round(hour.temperature),
      estadoCielo: hour.weatherStatus,
      probabilidadLluvia: hour.precipitationProbability,
      viento: Math.round(hour.windSpeed)
    }));
  }

  private mapProviderData(response: BackendWeatherForecastResponse): WeatherProviderData[] {
    if (!response.providerData) {
      return [];
    }

    return response.providerData.map(provider => ({
      provider: provider.provider,
      temperature: provider.temperature,
      humidity: provider.humidity,
      windSpeed: provider.windSpeed,
      precipitationProbability: provider.precipitationProbability,
      weatherStatus: provider.weatherStatus
    }));
  }

  private mapPlanRecommendation(response: BackendWeatherForecastResponse): PlanRecommendation {
    if (!response.planRecommendation) {
      return {
        category: this.getRecommendationCategory(response),
        confidence: 50,
        summary: response.currentRecommendation,
        recommendedPlanTypes: [
          'plan flexible',
          'actividad urbana',
          'alternativa cubierta'
        ],
        reasons: [
          'Recomendación generada mediante reglas internas del sistema.'
        ]
      };
    }

    return {
      category: response.planRecommendation.category,
      confidence: response.planRecommendation.confidence,
      summary: response.planRecommendation.summary,
      recommendedPlanTypes: response.planRecommendation.recommendedPlanTypes || [],
      reasons: response.planRecommendation.reasons || []
    };
  }

  private buildDescription(response: BackendWeatherForecastResponse): string {
    return `Temperatura actual de ${Math.round(response.currentTemperature)}°C, humedad del ${response.currentHumidity}% y viento de ${Math.round(response.currentWindSpeed)} km/h.`;
  }

  private getRecommendationCategory(response: BackendWeatherForecastResponse): string {
    const lluvia = response.currentPrecipitationProbability;
    const temperatura = response.currentTemperature;
    const viento = response.currentWindSpeed;
    const estado = response.currentWeatherStatus?.toLowerCase() || '';

    if (estado.includes('tormenta')) {
      return 'Precaución';
    }

    if (lluvia >= 60 || estado.includes('lluvia') || estado.includes('llovizna')) {
      return 'Interior';
    }

    if (viento >= 30) {
      return 'Mixta';
    }

    if (temperatura >= 30) {
      return 'Interior';
    }

    if (lluvia >= 30 || estado.includes('nuboso')) {
      return 'Mixta';
    }

    return 'Exterior';
  }

  private formatHour(time: string): string {
    if (!time || !time.includes('T')) {
      return time;
    }

    return time.split('T')[1];
  }
}
