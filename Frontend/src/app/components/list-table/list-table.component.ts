import { Component, OnInit } from '@angular/core';
import { WeatherData, WeatherService } from 'src/app/services/weather.service';

@Component({
  selector: 'app-list-table',
  templateUrl: './list-table.component.html',
  styleUrls: ['./list-table.component.css']
})
export class ListTableComponent implements OnInit {

  weatherList: WeatherData[] = [];

  constructor(private weatherService: WeatherService) { }

  ngOnInit(): void {
    this.getWeatherData();
  }

  getWeatherData(): void {
    this.weatherService.getWeatherList().subscribe({
      next: (result: WeatherData[]) => {
        this.weatherList = result;
      },
      error: () => {
        console.error('Error al cargar la tabla meteorológica');
      },
      complete: () => {
        console.log('Tabla meteorológica cargada correctamente');
      }
    });
  }

}
