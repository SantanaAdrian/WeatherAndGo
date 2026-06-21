import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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

  constructor(
    private weatherService: WeatherService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id') || this.weatherService.getActiveLocationId();
      this.loadWeather(id);
    });
  }

  getDeviceLocation(): void {
    this.weatherService.setActiveLocationId('ubicacion-actual');

    const currentId = this.route.snapshot.paramMap.get('id');

    if (currentId === 'ubicacion-actual') {
      this.loadWeather('ubicacion-actual');
      return;
    }

    this.router.navigate(['/home', 'ubicacion-actual']);
  }

  loadWeather(id: string): void {
    this.loading = true;
    this.locationError = '';
    this.weatherService.setActiveLocationId(id);

    this.weatherService.getForecastById(id).subscribe({
      next: (result: WeatherData | undefined) => {
        if (!result) {
          this.locationError = 'No se han podido cargar los datos meteorológicos.';
          this.weather = undefined;
          this.recommendedPlans = [];
          this.loading = false;
          return;
        }

        this.weather = result;
        this.generatePlans();
        this.loading = false;
      },
      error: () => {
        this.locationError = 'No se han podido cargar los datos meteorológicos.';
        this.weather = undefined;
        this.recommendedPlans = [];
        this.loading = false;
      }
    });
  }

  generatePlans(): void {
    if (!this.weather) {
      return;
    }

    if (this.weather.planRecommendation?.personalizedPlans?.length > 0) {
      this.recommendedPlans = this.weather.planRecommendation.personalizedPlans.slice(0, 3).map(plan => ({
        titulo: plan.title,
        categoria: plan.category,
        descripcion: plan.description
      }));

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
          titulo: 'Escape room o juego de interior',
          categoria: 'Ocio',
          descripcion: 'Alternativa cubierta para hacer algo diferente sin depender del tiempo.'
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
          titulo: 'Playa, zona de baño o paseo junto al agua',
          categoria: 'Exterior',
          descripcion: 'Buen plan de verano si no hay lluvia ni viento fuerte.'
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
