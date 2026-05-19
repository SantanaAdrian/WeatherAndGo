import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

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

export interface WeatherData {
  id: string;
  ciudad: string;
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
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {

  private weatherMock: WeatherData[] = [
    {
      id: 'bilbao',
      ciudad: 'Bilbao',
      fecha: '2026-05-18',
      temperatura: 18,
      humedad: 72,
      viento: 14,
      probabilidadLluvia: 65,
      estadoCielo: 'Lluvia débil',
      descripcion: 'Día fresco con posibilidad de lluvia intermitente.',
      recomendacion: 'Se recomienda realizar actividades bajo techo, como visitar un museo, ir al cine o entrenar en un gimnasio.',
      categoriaRecomendada: 'Interior',
      forecast: [
      {
        fecha: '2026-05-18',
        temperaturaMaxima: 18,
        temperaturaMinima: 12,
        humedad: 72,
        viento: 14,
        probabilidadLluvia: 65,
        estadoCielo: 'Lluvia débil',
        recomendacion: 'Mejor optar por planes de interior.'
      },
      {
        fecha: '2026-05-19',
        temperaturaMaxima: 19,
        temperaturaMinima: 13,
        humedad: 68,
        viento: 12,
        probabilidadLluvia: 45,
        estadoCielo: 'Nuboso',
        recomendacion: 'Plan mixto: paseo corto y alternativa cubierta.'
      },
      {
        fecha: '2026-05-20',
        temperaturaMaxima: 21,
        temperaturaMinima: 14,
        humedad: 60,
        viento: 10,
        probabilidadLluvia: 20,
        estadoCielo: 'Parcialmente nuboso',
        recomendacion: 'Buen día para actividades al aire libre.'
      },
      {
        fecha: '2026-05-21',
        temperaturaMaxima: 20,
        temperaturaMinima: 13,
        humedad: 66,
        viento: 16,
        probabilidadLluvia: 35,
        estadoCielo: 'Intervalos nubosos',
        recomendacion: 'Recomendable llevar plan alternativo bajo techo.'
      },
      {
        fecha: '2026-05-22',
        temperaturaMaxima: 17,
        temperaturaMinima: 11,
        humedad: 78,
        viento: 20,
        probabilidadLluvia: 70,
        estadoCielo: 'Lluvia',
        recomendacion: 'Priorizar actividades interiores.'
      },
      {
        fecha: '2026-05-23',
        temperaturaMaxima: 19,
        temperaturaMinima: 12,
        humedad: 70,
        viento: 13,
        probabilidadLluvia: 40,
        estadoCielo: 'Nuboso',
        recomendacion: 'Buen día para planes tranquilos, con opción de interior por si empeora el tiempo.'
      },
      {
        fecha: '2026-05-24',
        temperaturaMaxima: 21,
        temperaturaMinima: 13,
        humedad: 62,
        viento: 11,
        probabilidadLluvia: 20,
        estadoCielo: 'Parcialmente nuboso',
        recomendacion: 'Condiciones adecuadas para pasear o realizar actividades al aire libre.'
      }
    ]
    },
    {
      id: 'madrid',
      ciudad: 'Madrid',
      fecha: '2026-05-18',
      temperatura: 26,
      humedad: 38,
      viento: 10,
      probabilidadLluvia: 5,
      estadoCielo: 'Soleado',
      descripcion: 'Día cálido y seco, con cielo despejado durante la mayor parte de la jornada.',
      recomendacion: 'Condiciones adecuadas para pasear, hacer deporte moderado o realizar actividades al aire libre.',
      categoriaRecomendada: 'Exterior',
      forecast: [
        {
          fecha: '2026-05-18',
          temperaturaMaxima: 26,
          temperaturaMinima: 16,
          humedad: 38,
          viento: 10,
          probabilidadLluvia: 5,
          estadoCielo: 'Soleado',
          recomendacion: 'Buen día para planes al aire libre.'
        },
        {
          fecha: '2026-05-19',
          temperaturaMaxima: 28,
          temperaturaMinima: 17,
          humedad: 35,
          viento: 9,
          probabilidadLluvia: 0,
          estadoCielo: 'Despejado',
          recomendacion: 'Evitar las horas centrales si hace calor.'
        },
        {
          fecha: '2026-05-20',
          temperaturaMaxima: 30,
          temperaturaMinima: 18,
          humedad: 32,
          viento: 11,
          probabilidadLluvia: 0,
          estadoCielo: 'Muy soleado',
          recomendacion: 'Mejor planes en sombra o interiores climatizados.'
        },
        {
          fecha: '2026-05-21',
          temperaturaMaxima: 27,
          temperaturaMinima: 17,
          humedad: 40,
          viento: 13,
          probabilidadLluvia: 10,
          estadoCielo: 'Soleado',
          recomendacion: 'Buen día para rutas urbanas.'
        },
        {
          fecha: '2026-05-22',
          temperaturaMaxima: 25,
          temperaturaMinima: 15,
          humedad: 42,
          viento: 12,
          probabilidadLluvia: 15,
          estadoCielo: 'Parcialmente nuboso',
          recomendacion: 'Condiciones adecuadas para deporte suave.'
        },
        {
          fecha: '2026-05-23',
          temperaturaMaxima: 27,
          temperaturaMinima: 16,
          humedad: 39,
          viento: 10,
          probabilidadLluvia: 5,
          estadoCielo: 'Soleado',
          recomendacion: 'Buen día para actividades exteriores, evitando las horas de más calor.'
        },
        {
          fecha: '2026-05-24',
          temperaturaMaxima: 29,
          temperaturaMinima: 18,
          humedad: 34,
          viento: 9,
          probabilidadLluvia: 0,
          estadoCielo: 'Despejado',
          recomendacion: 'Recomendable hacer planes al aire libre por la mañana o al final de la tarde.'
        }
      ]
    },
    {
      id: 'barcelona',
      ciudad: 'Barcelona',
      fecha: '2026-05-18',
      temperatura: 23,
      humedad: 61,
      viento: 18,
      probabilidadLluvia: 20,
      estadoCielo: 'Parcialmente nuboso',
      descripcion: 'Temperatura agradable con intervalos nubosos y viento moderado.',
      recomendacion: 'Buen momento para planes urbanos, paseos cortos o actividades culturales al aire libre.',
      categoriaRecomendada: 'Mixta',
      forecast: [
        {
          fecha: '2026-05-18',
          temperaturaMaxima: 23,
          temperaturaMinima: 17,
          humedad: 61,
          viento: 18,
          probabilidadLluvia: 20,
          estadoCielo: 'Parcialmente nuboso',
          recomendacion: 'Buen día para planes urbanos.'
        },
        {
          fecha: '2026-05-19',
          temperaturaMaxima: 24,
          temperaturaMinima: 18,
          humedad: 58,
          viento: 15,
          probabilidadLluvia: 15,
          estadoCielo: 'Soleado',
          recomendacion: 'Adecuado para actividades al aire libre.'
        },
        {
          fecha: '2026-05-20',
          temperaturaMaxima: 22,
          temperaturaMinima: 17,
          humedad: 65,
          viento: 20,
          probabilidadLluvia: 35,
          estadoCielo: 'Nuboso',
          recomendacion: 'Plan mixto con alternativa cubierta.'
        },
        {
          fecha: '2026-05-21',
          temperaturaMaxima: 21,
          temperaturaMinima: 16,
          humedad: 70,
          viento: 22,
          probabilidadLluvia: 50,
          estadoCielo: 'Lluvia débil',
          recomendacion: 'Mejor planes de interior.'
        },
        {
          fecha: '2026-05-22',
          temperaturaMaxima: 24,
          temperaturaMinima: 18,
          humedad: 59,
          viento: 14,
          probabilidadLluvia: 10,
          estadoCielo: 'Despejado',
          recomendacion: 'Buen día para pasear.'
        },
        {
          fecha: '2026-05-23',
          temperaturaMaxima: 25,
          temperaturaMinima: 18,
          humedad: 57,
          viento: 12,
          probabilidadLluvia: 10,
          estadoCielo: 'Soleado',
          recomendacion: 'Buen día para paseos, rutas urbanas o actividades junto al mar.'
        },
        {
          fecha: '2026-05-24',
          temperaturaMaxima: 24,
          temperaturaMinima: 17,
          humedad: 63,
          viento: 16,
          probabilidadLluvia: 25,
          estadoCielo: 'Parcialmente nuboso',
          recomendacion: 'Plan exterior viable, aunque conviene revisar la previsión antes de salir.'
        }
      ]
    }
  ];

  constructor() { }

  getWeatherList(): Observable<WeatherData[]> {
    return of(this.weatherMock);
  }

  getWeatherById(id: string): Observable<WeatherData | undefined> {
    const weatherData = this.weatherMock.find(item => item.id === id);
    return of(weatherData);
  }

  getForecastById(id: string): Observable<WeatherData | undefined> {
    const weatherData = this.weatherMock.find(item => item.id === id);
    return of(weatherData);
  }
}
