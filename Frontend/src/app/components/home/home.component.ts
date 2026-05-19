import { Component, OnInit } from '@angular/core';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

interface RecommendedPlan {
  titulo: string;
  categoria: string;
  descripcion: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  weather?: WeatherData;
  recommendedPlans: RecommendedPlan[] = [];

  loading: boolean = false;
  locationError: string = '';

  constructor(private weatherService: WeatherService) { }

  ngOnInit(): void {
    this.getDeviceLocation();
  }

  getDeviceLocation(): void {
    this.loading = true;
    this.locationError = '';

    if (!navigator.geolocation) {
      this.locationError = 'El navegador no permite obtener la ubicación. Se muestran datos de Bilbao.';
      this.loadDefaultWeather();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position: GeolocationPosition) => {
        const latitud = position.coords.latitude;
        const longitud = position.coords.longitude;

        this.weatherService.getWeatherByCoordinates(
          latitud,
          longitud,
          'ubicacion-actual',
          'Tu ubicación actual'
        ).subscribe({
          next: (result: WeatherData) => {
            this.weather = result;
            this.generatePlans();
            this.loading = false;
          },
          error: () => {
            this.locationError = 'No se ha podido obtener la predicción de tu ubicación. Se muestran datos de Bilbao.';
            this.loadDefaultWeather();
          }
        });
      },
      () => {
        this.locationError = 'No se ha podido obtener la ubicación. Se muestran datos de Bilbao.';
        this.loadDefaultWeather();
      }
    );
  }

  loadDefaultWeather(): void {
    this.weatherService.getWeatherByCoordinates(
      43.263,
      -2.935,
      'bilbao',
      'Bilbao'
    ).subscribe({
      next: (result: WeatherData) => {
        this.weather = result;
        this.generatePlans();
        this.loading = false;
      },
      error: () => {
        this.locationError = 'No se han podido cargar los datos meteorológicos.';
        this.loading = false;
      }
    });
  }

  generatePlans(): void {
    if (!this.weather) {
      return;
    }

    if (this.weather.probabilidadLluvia >= 60 || this.weather.estadoCielo.toLowerCase().includes('lluvia')) {
      this.recommendedPlans = [
        {
          titulo: 'Visitar un museo o exposición',
          categoria: 'Cultura',
          descripcion: 'Plan recomendado para evitar la lluvia y aprovechar el día en un espacio cubierto.'
        },
        {
          titulo: 'Entrenamiento en gimnasio',
          categoria: 'Deporte',
          descripcion: 'Alternativa adecuada si las condiciones no permiten realizar deporte al aire libre.'
        },
        {
          titulo: 'Cine o actividad interior',
          categoria: 'Ocio',
          descripcion: 'Opción cómoda para días con alta probabilidad de lluvia.'
        }
      ];

      return;
    }

    if (this.weather.estadoCielo.toLowerCase().includes('tormenta')) {
      this.recommendedPlans = [
        {
          titulo: 'Plan de interior',
          categoria: 'Interior',
          descripcion: 'Recomendado por posible tormenta o condiciones inestables.'
        },
        {
          titulo: 'Actividad cultural cubierta',
          categoria: 'Cultura',
          descripcion: 'Opción segura para evitar exposición al mal tiempo.'
        },
        {
          titulo: 'Ocio en espacio cerrado',
          categoria: 'Ocio',
          descripcion: 'Alternativa adecuada si la previsión empeora durante el día.'
        }
      ];

      return;
    }

    if (this.weather.temperatura >= 30) {
      this.recommendedPlans = [
        {
          titulo: 'Paseo en zona sombreada',
          categoria: 'Exterior',
          descripcion: 'Actividad ligera recomendada evitando las horas centrales del día.'
        },
        {
          titulo: 'Plan en terraza o zona fresca',
          categoria: 'Ocio',
          descripcion: 'Buena opción para aprovechar el buen tiempo sin exposición excesiva al calor.'
        },
        {
          titulo: 'Actividad interior climatizada',
          categoria: 'Interior',
          descripcion: 'Recomendación segura para evitar temperaturas elevadas.'
        }
      ];

      return;
    }

    this.recommendedPlans = [
      {
        titulo: 'Paseo al aire libre',
        categoria: 'Exterior',
        descripcion: 'Condiciones adecuadas para caminar o realizar actividad moderada.'
      },
      {
        titulo: 'Ruta urbana',
        categoria: 'Ocio',
        descripcion: 'Plan recomendado para disfrutar de la ciudad con condiciones meteorológicas favorables.'
      },
      {
        titulo: 'Deporte suave',
        categoria: 'Deporte',
        descripcion: 'Buena opción si el viento y la lluvia se mantienen bajos.'
      }
    ];
  }

}
