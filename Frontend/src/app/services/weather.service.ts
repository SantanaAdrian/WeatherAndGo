import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

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

export interface PersonalizedPlan {
  title: string;
  category: string;
  description: string;
  placeName?: string;
  address?: string;
  externalUrl?: string;
}

export interface PlanRecommendation {
  category: string;
  confidence: number;
  summary: string;
  recommendedPlanTypes: string[];
  reasons: string[];
  personalizedPlans: PersonalizedPlan[];
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

interface BackendPersonalizedPlan {
  title: string;
  category: string;
  description: string;
  placeName?: string;
  address?: string;
  externalUrl?: string;
}

interface BackendPlanRecommendation {
  category: string;
  confidence: number;
  summary: string;
  recommendedPlanTypes: string[];
  reasons: string[];
  personalizedPlans?: BackendPersonalizedPlan[];
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

interface GeocodingApiResult {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  country?: string;
  admin1?: string;
  admin2?: string;
}

interface GeocodingApiResponse {
  results?: GeocodingApiResult[];
}

export interface LocationSuggestion {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private readonly backendUrl = 'http://localhost:8080/api/weather/forecast';
  private readonly logsUrl = 'http://localhost:8080/api/weather/logs';
  private readonly geocodingUrl = 'https://geocoding-api.open-meteo.com/v1/search';
  private readonly activeLocationStorageKey = 'weatherandgo-active-location-id';
  private readonly customLocationStorageKey = 'weatherandgo-custom-location';

  private readonly cityLocations: CityLocation[] = [
    { id: 'bilbao', ciudad: 'Bilbao', lat: 43.263, lon: -2.935 },
    { id: 'madrid', ciudad: 'Madrid', lat: 40.4168, lon: -3.7038 },
    { id: 'barcelona', ciudad: 'Barcelona', lat: 41.3874, lon: 2.1686 },
    { id: 'valencia', ciudad: 'Valencia', lat: 39.4699, lon: -0.3763 },
    { id: 'sevilla', ciudad: 'Sevilla', lat: 37.3891, lon: -5.9845 },
    { id: 'santander', ciudad: 'Santander', lat: 43.4623, lon: -3.8099 }
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
    if (id === 'ubicacion-actual') {
      return this.getCurrentLocationWeather().pipe(
        catchError(error => {
          console.error('Error obteniendo ubicación actual', error);

          return this.getWeatherByCoordinates(
            43.263,
            -2.935,
            'bilbao',
            'Bilbao'
          );
        })
      );
    }

    const city = this.cityLocations.find(item => item.id === id);

    if (city) {
      return this.getWeatherByCoordinates(city.lat, city.lon, city.id, city.ciudad);
    }

    const customLocation = this.parseCustomLocationId(id);

    if (customLocation) {
      return this.getWeatherByCoordinates(
        customLocation.lat,
        customLocation.lon,
        id,
        customLocation.ciudad
      );
    }

    return of(undefined);
  }

  getCurrentLocationWeather(): Observable<WeatherData> {
    return new Observable<WeatherData>((observer) => {
      if (!navigator.geolocation) {
        observer.error('El navegador no permite obtener la ubicación.');
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position: GeolocationPosition) => {
          this.getWeatherByCoordinates(
            position.coords.latitude,
            position.coords.longitude,
            'ubicacion-actual',
            'Tu ubicación actual'
          ).subscribe({
            next: (result: WeatherData) => {
              observer.next(result);
              observer.complete();
            },
            error: (error) => {
              observer.error(error);
            }
          });
        },
        (error) => {
          observer.error(error);
        }
      );
    });
  }

  searchLocation(query: string): Observable<WeatherData | undefined> {
    const cleanQuery = query.trim();

    if (!cleanQuery) {
      return of(undefined);
    }

    const url = `${this.geocodingUrl}?name=${encodeURIComponent(cleanQuery)}&count=1&language=es&format=json`;

    return this.http.get<GeocodingApiResponse>(url).pipe(
      switchMap((response: GeocodingApiResponse) => {
        const result = response.results && response.results.length > 0
          ? response.results[0]
          : undefined;

        if (!result) {
          return of(undefined);
        }

        const ciudad = this.buildLocationName(result);
        const id = this.buildCustomLocationId(result.name, result.latitude, result.longitude);

        this.saveCustomLocation(id, ciudad, result.latitude, result.longitude);
        this.setActiveLocationId(id);

        return this.getWeatherByCoordinates(
          result.latitude,
          result.longitude,
          id,
          ciudad
        );
      }),
      catchError(error => {
        console.error('Error buscando ubicación', error);
        return of(undefined);
      })
    );
  }

  searchLocationSuggestions(query: string): Observable<LocationSuggestion[]> {
  const cleanQuery = query.trim();

  if (cleanQuery.length < 2) {
    return of([]);
  }

  const url = `${this.geocodingUrl}?name=${encodeURIComponent(cleanQuery)}&count=6&language=es&format=json`;

  return this.http.get<GeocodingApiResponse>(url).pipe(
    map((response: GeocodingApiResponse) => {
      if (!response.results) {
        return [];
      }

      return response.results.map(result => {
        const name = this.buildLocationName(result);
        const id = this.buildCustomLocationId(result.name, result.latitude, result.longitude);

        return {
          id,
          name,
          latitude: result.latitude,
          longitude: result.longitude
        };
      });
    }),
    catchError(error => {
      console.error('Error obteniendo sugerencias de ubicación', error);
      return of([]);
    })
  );
  }

  getActiveLocationId(): string {
    return sessionStorage.getItem(this.activeLocationStorageKey) || 'ubicacion-actual';
  }

  setActiveLocationId(id: string): void {
    sessionStorage.setItem(this.activeLocationStorageKey, id);
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
        ],
        personalizedPlans: [
          {
            title: 'Paseo urbano flexible',
            category: 'MIXTO',
            description: 'Plan adaptable a condiciones meteorológicas favorables, manteniendo una alternativa cubierta cercana.',
            placeName: 'Ruta urbana cercana',
            address: response.locationName || 'Zona consultada',
            externalUrl: this.buildGoogleSearchUrl(`ruta urbana ${response.locationName || ''}`)
          },
          {
            title: 'Actividad interior cercana',
            category: 'INTERIOR',
            description: 'Alternativa recomendada si cambia la previsión o aumenta la incertidumbre meteorológica.',
            placeName: 'Escape room, recreativos o cafetería cercana',
            address: response.locationName || 'Zona consultada',
            externalUrl: this.buildGoogleSearchUrl(`escape room recreativos cafetería ${response.locationName || ''}`)
          },
          {
            title: 'Zona de agua, parque o terraza',
            category: 'EXTERIOR',
            description: 'Plan recomendable con buen tiempo o calor, especialmente si hay playa, río, parque o zona fresca cercana.',
            placeName: 'Zona exterior cercana',
            address: response.locationName || 'Zona consultada',
            externalUrl: this.buildGoogleSearchUrl(`playa parque terraza cerca de ${response.locationName || ''}`)
          }
        ]
      };
    }

    return {
      category: response.planRecommendation.category,
      confidence: response.planRecommendation.confidence,
      summary: response.planRecommendation.summary,
      recommendedPlanTypes: response.planRecommendation.recommendedPlanTypes || [],
      reasons: response.planRecommendation.reasons || [],
      personalizedPlans: (response.planRecommendation.personalizedPlans || []).map(plan => ({
        title: plan.title,
        category: plan.category,
        description: plan.description,
        placeName: plan.placeName,
        address: plan.address,
        externalUrl: plan.externalUrl
      }))
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
      return 'Mixta';
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

  private buildGoogleSearchUrl(query: string): string {
    return `https://www.google.com/search?q=${encodeURIComponent(query)}`;
  }

  private buildLocationName(result: GeocodingApiResult): string {
    const parts = [
      result.name,
      result.admin2,
      result.admin1,
      result.country
    ].filter(part => !!part);

    return parts.join(', ');
  }

  private buildCustomLocationId(name: string, lat: number, lon: number): string {
    const slug = name
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '') || 'ubicacion';

    return `loc_${lat.toFixed(5)}_${lon.toFixed(5)}_${slug}`;
  }

  private saveCustomLocation(id: string, ciudad: string, lat: number, lon: number): void {
    sessionStorage.setItem(
      this.customLocationStorageKey,
      JSON.stringify({
        id,
        ciudad,
        lat,
        lon
      })
    );
  }

  private parseCustomLocationId(id: string): CityLocation | undefined {
    const savedLocationText = sessionStorage.getItem(this.customLocationStorageKey);

    if (savedLocationText) {
      try {
        const savedLocation = JSON.parse(savedLocationText);

        if (savedLocation.id === id) {
          return {
            id: savedLocation.id,
            ciudad: savedLocation.ciudad,
            lat: savedLocation.lat,
            lon: savedLocation.lon
          };
        }
      } catch {
        sessionStorage.removeItem(this.customLocationStorageKey);
      }
    }

    const match = id.match(/^loc_(-?\d+(?:\.\d+)?)_(-?\d+(?:\.\d+)?)(?:_(.+))?$/);

    if (!match) {
      return undefined;
    }

    const lat = Number(match[1]);
    const lon = Number(match[2]);
    const slug = match[3] || 'ubicacion';

    if (Number.isNaN(lat) || Number.isNaN(lon)) {
      return undefined;
    }

    return {
      id,
      ciudad: this.slugToLocationName(slug),
      lat,
      lon
    };
  }

  private slugToLocationName(slug: string): string {
    return slug
      .split('-')
      .filter(part => part)
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }
}
