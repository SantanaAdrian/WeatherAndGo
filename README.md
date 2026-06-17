# Weather&Go

Weather&Go es una plataforma web meteorológica multiproveedor desarrollada como Trabajo de Fin de Grado del Grado en Ingeniería Informática de la Universidad Internacional de La Rioja.

La aplicación permite consultar datos meteorológicos, comparar información procedente de varios proveedores externos y generar recomendaciones inteligentes de planes según las condiciones climáticas.

## Descripción general

Weather&Go integra datos meteorológicos procedentes de diferentes APIs públicas, los normaliza en un modelo común, calcula una predicción agregada y genera recomendaciones de actividades mediante un motor inteligente propio desarrollado en Python.

El sistema está dividido en tres módulos principales:

```text
WeatherAndGo
├── Backend
├── Frontend
└── RecommendationEngine
```

## Funcionalidades principales

```text
Consulta meteorológica por ubicación.
Predicción actual, por horas y por días.
Integración de varios proveedores meteorológicos.
Comparativa entre proveedores.
Cálculo de valores agregados.
Resumen de fiabilidad de la predicción.
Persistencia de consultas en MySQL.
Motor inteligente de recomendaciones en Python.
Recomendaciones de planes según lluvia, viento, temperatura y fiabilidad.
Interfaz web desarrollada con Angular.
API REST desarrollada con Spring Boot.
```

## Proveedores integrados

```text
Open-Meteo
OpenWeather
MET Norway
Nominatim
```

Open-Meteo se utiliza como proveedor principal para predicción horaria y diaria.

OpenWeather se utiliza como fuente adicional para datos actuales.

MET Norway se utiliza como tercer proveedor de contraste.

Nominatim se utiliza para resolver nombres de ubicación a partir de coordenadas.

## Arquitectura

La arquitectura del sistema sigue una separación por responsabilidades.

```text
Frontend Angular
        |
        v
Backend Spring Boot
        |
        |---- Open-Meteo
        |---- OpenWeather
        |---- MET Norway
        |---- Nominatim
        |
        |---- MySQL
        |
        v
RecommendationEngine Python
```

El frontend no consulta directamente los proveedores meteorológicos.

El backend centraliza la integración externa, la normalización de datos, la agregación, la persistencia y la comunicación con el motor inteligente.

El motor Python recibe los datos meteorológicos ya tratados y devuelve una recomendación estructurada.

## Tecnologías utilizadas

```text
Java
Spring Boot
Angular
TypeScript
Python
FastAPI
Pydantic
MySQL
HTML
CSS
Bootstrap
Git
GitHub
```

## Requisitos previos

Para ejecutar el proyecto es necesario tener instalado:

```text
Java 17 o superior
Maven
Node.js
Angular CLI
Python 3.10 o superior
MySQL
Git
```

## Configuración de base de datos

Crear una base de datos MySQL para el proyecto.

```sql
CREATE DATABASE weatherandgo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'weathergo_user'@'localhost' IDENTIFIED BY 'weathergo_pass';

GRANT ALL PRIVILEGES ON weatherandgo.* TO 'weathergo_user'@'localhost';

FLUSH PRIVILEGES;
```

## Configuración del backend

El backend se encuentra en:

```text
Backend
```

Ejemplo de configuración en `application.properties`:

```properties
spring.application.name=weatherandgo-backend

spring.datasource.url=jdbc:mysql://localhost:3306/weatherandgo
spring.datasource.username=weathergo_user
spring.datasource.password=weathergo_pass

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

openweather.api.key=TU_API_KEY_DE_OPENWEATHER

recommendation.engine.url=http://localhost:8001
```

Por seguridad, no se deben subir claves reales al repositorio público.

## Ejecución del backend

Desde la carpeta del backend:

```bash
cd Backend
mvn spring-boot:run
```

El backend se ejecuta por defecto en:

```text
http://localhost:8080
```

Endpoint principal:

```text
GET http://localhost:8080/api/weather/forecast?lat=43.2806&lon=-2.9809
```

## Configuración del frontend

El frontend se encuentra en:

```text
Frontend
```

Instalar dependencias:

```bash
cd Frontend
npm install
```

Ejecutar Angular:

```bash
ng serve
```

El frontend se ejecuta por defecto en:

```text
http://localhost:4200
```

## Configuración del motor de recomendaciones

El motor de recomendaciones se encuentra en:

```text
RecommendationEngine
```

Crear entorno virtual:

```bash
cd RecommendationEngine
python -m venv venv
```

Activar entorno virtual en Windows:

```bash
venv\Scripts\activate
```

Instalar dependencias:

```bash
pip install -r requirements.txt
```

Ejecutar el microservicio:

```bash
uvicorn main:app --reload --port 8001
```

El motor de recomendaciones se ejecuta en:

```text
http://localhost:8001
```

Endpoint de comprobación:

```text
GET http://localhost:8001/health
```

Endpoint principal:

```text
POST http://localhost:8001/recommendations/weather-plan
```

## Funcionamiento del motor inteligente

El motor Python recibe datos meteorológicos ya normalizados por el backend.

Analiza variables como:

```text
Temperatura
Humedad
Viento
Probabilidad de lluvia
Estado meteorológico
Fiabilidad de la agregación
Diferencias entre proveedores
```

A partir de esos datos clasifica la situación en una de estas categorías:

```text
EXTERIOR
INTERIOR
MIXTO
PRECAUCION
```

La respuesta incluye:

```text
Categoría recomendada
Nivel de confianza
Resumen explicativo
Tipos de plan recomendados
Motivos de la recomendación
Planes personalizados
```

Ejemplo de salida:

```json
{
  "category": "MIXTO",
  "confidence": 52,
  "summary": "Se recomienda un plan mixto con alternativa cubierta.",
  "recommendedPlanTypes": [
    "paseo corto",
    "cafetería",
    "plan urbano",
    "actividad cubierta alternativa"
  ],
  "reasons": [
    "La probabilidad de lluvia es baja.",
    "El viento es moderado y puede afectar a planes exteriores prolongados.",
    "La fiabilidad de la predicción agregada es media."
  ]
}
```

## Pantallas principales

```text
Inicio
Predicción meteorológica
Comparativa de proveedores
Recomendaciones inteligentes
```

La pantalla de inicio muestra un resumen meteorológico.

La pantalla de predicción muestra datos actuales, predicción por horas y predicción por días.

La pantalla de comparativa permite comparar los valores de cada proveedor.

La pantalla de recomendaciones muestra la salida generada por el motor inteligente.

## Estructura del repositorio

```text
WeatherAndGo
├── Backend
│   ├── src
│   └── pom.xml
│
├── Frontend
│   ├── src
│   ├── angular.json
│   └── package.json
│
├── RecommendationEngine
│   ├── app
│   ├── main.py
│   └── requirements.txt
│
├── .gitignore
└── README.md
```

## Endpoints principales

Backend:

```text
GET /api/weather/forecast
GET /api/weather/logs
GET /api/weather/logs/latest
GET /api/weather/logs/count
DELETE /api/weather/logs
```

RecommendationEngine:

```text
GET /health
POST /recommendations/weather-plan
```

## Estado del proyecto

Proyecto académico funcional desarrollado como Trabajo de Fin de Grado.

El sistema implementa una arquitectura completa con frontend, backend, base de datos y motor inteligente independiente.

## Autor

```text
Adrian Santana Salinas
Grado en Ingeniería Informática
Universidad Internacional de La Rioja
```

## Repositorio

```text
https://github.com/SantanaAdrian/WeatherAndGo
```

## Licencia

Este proyecto se publica bajo licencia MIT.

Consultar el archivo `LICENSE` para más información.
