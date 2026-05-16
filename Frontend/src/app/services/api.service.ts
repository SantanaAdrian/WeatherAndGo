// src/app/services/api.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PokemonAdapterService } from './pokemon-adapter.service';
import { WeatherService } from './weather.service';
import { LocationModel } from '../models/location.model';
import { Observation } from '../models/observation.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  // Inyecta ambos: adapter legacy y el servicio real (cuando esté listo)
  constructor(
    private pokemonAdapter: PokemonAdapterService,
    private weatherService: WeatherService
  ) {}

  // Para el MVP usa el adapter; cuando el backend esté listo, cambia a weatherService
  getLocations(useLegacy = true): Observable<LocationModel[]> {
    return useLegacy ? this.pokemonAdapter.getLocationsFromPokemons(50) : this.weatherService.getLocations();
  }

  getObservations(locationId: string, useLegacy = true): Observable<Observation[]> {
    return useLegacy ? this.pokemonAdapter.getHistoricalObservationsFromPokemon(locationId, 24) : this.weatherService.getObservations(locationId);
  }

  ingestManual(payload: any) {
    return this.weatherService.ingestManual(payload);
  }

  ping() {
    return this.weatherService.ping();
  }
}
