import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, filter, switchMap } from 'rxjs/operators';
import { LocationSuggestion, WeatherData, WeatherService } from 'src/app/services/weather.service';

@Component({
  selector: 'app-menu-principal',
  templateUrl: './menu-principal.component.html',
  styleUrls: ['./menu-principal.component.css']
})
export class MenuPrincipalComponent implements OnInit {
  searchText: string = '';
  activeLocationId: string = 'ubicacion-actual';
  loading: boolean = false;
  suggestions: LocationSuggestion[] = [];
  showSuggestions: boolean = false;

  private searchSubject = new Subject<string>();

  constructor(
    private router: Router,
    private weatherService: WeatherService
  ) { }

  ngOnInit(): void {
    this.activeLocationId = this.weatherService.getActiveLocationId();

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        const idFromUrl = this.getLocationIdFromCurrentUrl();

        if (idFromUrl) {
          this.activeLocationId = idFromUrl;
          this.weatherService.setActiveLocationId(idFromUrl);
        }
      });

    this.searchSubject.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(query => this.weatherService.searchLocationSuggestions(query).pipe(
        catchError(() => of([]))
      ))
    ).subscribe((results: LocationSuggestion[]) => {
      this.suggestions = results;
      this.showSuggestions = results.length > 0;
    });
  }

  onSearchTextChange(): void {
    this.searchSubject.next(this.searchText);
  }

  searchLocation(): void {
    const query = this.searchText.trim();

    if (!query) {
      return;
    }

    const exactSuggestion = this.suggestions.find(item =>
      item.name.toLowerCase() === query.toLowerCase()
    );

    if (exactSuggestion) {
      this.selectSuggestion(exactSuggestion);
      return;
    }

    this.loading = true;
    this.showSuggestions = false;

    this.weatherService.searchLocation(query).subscribe({
      next: (weather: WeatherData | undefined) => {
        this.loading = false;

        if (!weather) {
          alert('No se ha encontrado esa ubicación.');
          return;
        }

        this.activeLocationId = weather.id;
        this.weatherService.setActiveLocationId(weather.id);
        this.searchText = '';
        this.suggestions = [];

        const section = this.getCurrentSection();
        this.router.navigate([section, weather.id]);
      },
      error: () => {
        this.loading = false;
        alert('No se ha podido buscar la ubicación.');
      }
    });
  }

  selectSuggestion(suggestion: LocationSuggestion): void {
    this.loading = true;
    this.showSuggestions = false;

    this.weatherService.setActiveLocationId(suggestion.id);

    this.weatherService.getWeatherByCoordinates(
      suggestion.latitude,
      suggestion.longitude,
      suggestion.id,
      suggestion.name
    ).subscribe({
      next: () => {
        this.loading = false;
        this.activeLocationId = suggestion.id;
        this.searchText = '';
        this.suggestions = [];

        const section = this.getCurrentSection();
        this.router.navigate([section, suggestion.id]);
      },
      error: () => {
        this.loading = false;
        alert('No se ha podido cargar esa ubicación.');
      }
    });
  }

  hideSuggestionsLater(): void {
    setTimeout(() => {
      this.showSuggestions = false;
    }, 200);
  }

  private getCurrentSection(): string {
    const currentUrl = this.router.url;

    if (currentUrl.startsWith('/list-table')) {
      return '/list-table';
    }

    if (currentUrl.startsWith('/list')) {
      return '/list';
    }

    if (currentUrl.startsWith('/recommendations')) {
      return '/recommendations';
    }

    return '/home';
  }

  private getLocationIdFromCurrentUrl(): string | null {
    const parts = this.router.url.split('?')[0].split('/').filter(part => part);

    if (parts.length < 2) {
      return null;
    }

    const section = parts[0];

    if (
      section !== 'home'
      && section !== 'list'
      && section !== 'list-table'
      && section !== 'recommendations'
    ) {
      return null;
    }

    return parts[1];
  }
}
