import { Component, OnInit } from '@angular/core';

interface HomeWeather {
  ciudad: string;
  latitud: number;
  longitud: number;
  temperatura: number;
  humedad: number;
  viento: number;
  probabilidadLluvia: number;
  estadoCielo: string;
  descripcion: string;
}

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

  weather?: HomeWeather;
  recommendedPlans: RecommendedPlan[] = [];

  loading: boolean = false;
  locationError: string = '';

  ngOnInit(): void {
    this.getDeviceLocation();
  }

  getDeviceLocation(): void {
    this.loading = true;
    this.locationError = '';

    if (!navigator.geolocation) {
      this.loading = false;
      this.locationError = 'El navegador no permite obtener la ubicación.';
      this.loadDefaultWeather();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position: GeolocationPosition) => {
        const latitud = position.coords.latitude;
        const longitud = position.coords.longitude;

        this.loadMockWeatherByLocation(latitud, longitud);
        this.loading = false;
      },
      (error: GeolocationPositionError) => {
        this.loading = false;
        this.locationError = 'No se ha podido obtener la ubicación. Se muestran datos de ejemplo.';
        this.loadDefaultWeather();
      }
    );
  }

  loadMockWeatherByLocation(latitud: number, longitud: number): void {
    this.weather = {
      ciudad: 'Tu ubicación actual',
      latitud: latitud,
      longitud: longitud,
      temperatura: 18,
      humedad: 72,
      viento: 14,
      probabilidadLluvia: 65,
      estadoCielo: 'Lluvia débil',
      descripcion: 'Predicción simulada generada a partir de la ubicación del dispositivo.'
    };

    this.generatePlans();
  }

  loadDefaultWeather(): void {
    this.weather = {
      ciudad: 'Bilbao',
      latitud: 43.263,
      longitud: -2.935,
      temperatura: 18,
      humedad: 72,
      viento: 14,
      probabilidadLluvia: 65,
      estadoCielo: 'Lluvia débil',
      descripcion: 'Datos de ejemplo utilizados cuando no se dispone de ubicación.'
    };

    this.generatePlans();
  }

  generatePlans(): void {
    if (!this.weather) {
      return;
    }

    if (this.weather.probabilidadLluvia >= 60) {
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

    if (this.weather.temperatura >= 28) {
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
