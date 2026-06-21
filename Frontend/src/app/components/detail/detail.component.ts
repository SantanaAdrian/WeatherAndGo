import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit {

  weatherData?: WeatherData;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private weatherService: WeatherService
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      this.router.navigate(['error']);
      return;
    }

    this.getWeatherData(id);
  }

  getWeatherData(id: string): void {
    this.weatherService.getWeatherById(id).subscribe({
      next: (result: WeatherData | undefined) => {
        if (!result) {
          this.router.navigate(['error']);
          return;
        }

        this.weatherData = result;
      },
      error: () => {
        this.router.navigate(['error']);
      },
      complete: () => {
        console.log('Detalle meteorológico cargado correctamente');
      }
    });
  }

}
