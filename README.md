# AGriFleet Task 5 Backend

This repository contains the Task 5 backend service for the AGriFleet system. It provides the farm-selection and route-optimization logic used by the frontend and integrates with the core AGriFleet services for booking and farm data.

## Overview

The service runs on port 8085 and exposes a set of REST endpoints under the `/api/v1` namespace. It supports:

- Farm opportunity selection using weighted acreage and value scoring
- Exact or heuristic tour optimization for visiting selected farms
- Genetic algorithm optimization for larger or more flexible routing problems
- Integration with the core backend via the AGriFleet core API on port 8080

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Maven
- SQLite database
- Spring Web MVC and JPA

## Project Structure

```text
agrifleet-backend-task5/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/agrifleettask5/
│   │   │       ├── algorithm/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
├── README.md
├── AG18_TECHNICAL_NOTES.md
└── target/
```

## Configuration

The service configuration is stored in `src/main/resources/application.properties`.

```properties
spring.application.name=routing
server.port=8085
spring.datasource.url=jdbc:sqlite:../AgriFleet.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

The service is configured to accept requests from the frontend origins:

- http://localhost:3000
- http://127.0.0.1:3000

## Running the Service

### Prerequisites

- Java 17+
- Maven or the included Maven wrapper

### Start locally

```bash
./mvnw spring-boot:run
```

Or build and run the packaged JAR:

```bash
./mvnw clean package
java -jar target/agrifleettask5-0.0.1-SNAPSHOT.jar
```

The backend will start at:

```text
http://localhost:8085
```

## API Endpoints

### 1) Farm selection

Base path: `/api/v1/selection`

#### GET `/api/v1/selection/farms`

Returns available farm opportunities by fetching booking data from the core service at `http://localhost:8080/api/v1/bookings`.

Response example:

```json
[
  {
    "id": 101,
    "name": "Booking #101",
    "acreageHa": 42.5,
    "bookingValue": 12500.0,
    "cropType": "Wheat"
  }
]
```

#### POST `/api/v1/selection/maximize-acreage-value`

Selects the most valuable farms using weighted scoring.

Request body:

```json
{
  "availableFarms": [
    {
      "id": 101,
      "name": "Booking #101",
      "acreageHa": 42.5,
      "bookingValue": 12500.0,
      "cropType": "Wheat"
    },
    {
      "id": 102,
      "name": "Booking #102",
      "acreageHa": 28.0,
      "bookingValue": 9800.0,
      "cropType": "Maize"
    }
  ],
  "maxFarms": 2,
  "acreageWeight": 0.5,
  "bookingValueWeight": 0.5
}
```

Response example:

```json
{
  "selectedFarms": [
    {
      "id": 101,
      "name": "Booking #101",
      "acreageHa": 42.5,
      "bookingValue": 12500.0,
      "cropType": "Wheat"
    }
  ],
  "totalAcreageHa": 42.5,
  "totalBookingValue": 12500.0,
  "objectiveScore": 0.77,
  "algorithm": "AG19_WEIGHTED_SCORE",
  "objective": "maximize acreage and booking value"
}
```

---

### 2) Tour optimization

Base path: `/api/v1/tours`

#### POST `/api/v1/tours/optimize-sequence`

Optimizes a farm visit sequence based on depot and farm locations.

Request body:

```json
{
  "depot": {
    "id": 0,
    "name": "Depot A",
    "latitude": 7.8731,
    "longitude": 80.7718
  },
  "farms": [
    {
      "id": 1,
      "name": "Farm 1",
      "latitude": 7.887,
      "longitude": 80.784
    },
    {
      "id": 2,
      "name": "Farm 2",
      "latitude": 7.9001,
      "longitude": 80.7605
    }
  ],
  "distanceMatrix": [
    [0.0, 4.1, 5.6],
    [4.1, 0.0, 3.8],
    [5.6, 3.8, 0.0]
  ],
  "returnToDepot": true,
  "fuelConsumptionLitresPerKm": 0.25
}
```

Response example:

```json
{
  "visitSequence": [
    { "id": 0, "name": "Depot A", "latitude": 7.8731, "longitude": 80.7718 },
    { "id": 1, "name": "Farm 1", "latitude": 7.887, "longitude": 80.784 },
    { "id": 2, "name": "Farm 2", "latitude": 7.9001, "longitude": 80.7605 },
    { "id": 0, "name": "Depot A", "latitude": 7.8731, "longitude": 80.7718 }
  ],
  "legs": [
    { "fromId": 0, "toId": 1, "distanceKm": 4.1 },
    { "fromId": 1, "toId": 2, "distanceKm": 3.8 },
    { "fromId": 2, "toId": 0, "distanceKm": 5.6 }
  ],
  "totalDistanceKm": 13.5,
  "estimatedFuelLitres": 3.375,
  "algorithm": "Held-Karp (exact)",
  "optimalityGuaranteed": true,
  "timeComplexity": "Theta(n^2 * 2^n)",
  "spaceComplexity": "Theta(n * 2^n)",
  "executionTimeNanos": 124000
}
```

Notes:

- If `distanceMatrix` is omitted, the service calculates distances using Haversine distance between coordinates.
- If there are more than 18 farms, the service falls back to a nearest-neighbour heuristic.
- `returnToDepot` defaults to `true`.

---

### 3) Genetic algorithm optimization

Base path: `/api/v1/sequence`

#### POST `/api/v1/sequence/optimize-genetic-algorithm`

Runs a genetic algorithm for optimized farm ordering.

Request body:

```json
{
  "depot": {
    "id": 0,
    "name": "Depot A",
    "latitude": 7.8731,
    "longitude": 80.7718
  },
  "farms": [
    { "id": 1, "name": "Farm 1", "latitude": 7.887, "longitude": 80.784 },
    { "id": 2, "name": "Farm 2", "latitude": 7.9001, "longitude": 80.7605 }
  ],
  "distanceMatrix": [
    [0.0, 4.1, 5.6],
    [4.1, 0.0, 3.8],
    [5.6, 3.8, 0.0]
  ],
  "returnToDepot": true,
  "fuelConsumptionLitresPerKm": 0.25,
  "populationSize": 100,
  "generations": 200,
  "mutationRate": 0.02
}
```

Response example:

```json
{
  "visitSequence": [
    { "id": 0, "name": "Depot A", "latitude": 7.8731, "longitude": 80.7718 },
    { "id": 1, "name": "Farm 1", "latitude": 7.887, "longitude": 80.784 },
    { "id": 2, "name": "Farm 2", "latitude": 7.9001, "longitude": 80.7605 },
    { "id": 0, "name": "Depot A", "latitude": 7.8731, "longitude": 80.7718 }
  ],
  "legs": [
    { "fromId": 0, "toId": 1, "distanceKm": 4.1 },
    { "fromId": 1, "toId": 2, "distanceKm": 3.8 },
    { "fromId": 2, "toId": 0, "distanceKm": 5.6 }
  ],
  "totalDistanceKm": 13.5,
  "estimatedFuelLitres": 3.375,
  "algorithm": "Genetic Algorithm",
  "timeComplexity": "O(g * p * n^2)",
  "spaceComplexity": "O(p * n)",
  "elapsedNanoseconds": 2450000
}
```

Default values if omitted:

- `populationSize`: 100
- `generations`: 200
- `mutationRate`: 0.02

## Algorithm Behavior

### Selection logic

The `FarmSelectionService` reads bookings from the core service, filters valid booking records, and ranks farm opportunities using normalized acreage and booking value weights.

### Tour optimization logic

- Up to 18 farms: exact optimization using Held-Karp dynamic programming
- More than 18 farms: nearest-neighbour heuristic
- Open routes are allowed when `returnToDepot` is false

### Genetic optimization

The genetic algorithm implementation is designed for larger route sets where exact approaches are computationally heavy. It returns a candidate route with distance and fuel estimates.

## Validation Rules

The service validates input before optimization:

- depot must be present
- location names cannot be blank
- coordinates must be valid latitude and longitude values
- farm IDs must be unique
- `distanceMatrix` must be square and non-negative
- fuel rate must be non-negative
- `mutationRate` must be between 0 and 1

Invalid payloads return HTTP 400 responses through the error handler.

## Notes

- This service is intended for Task 5 only and corresponds to the AG-18/AG-19/AG-20 tour-planning flow.
- More detailed technical notes are available in `AG18_TECHNICAL_NOTES.md`.
- The frontend expects the backend on port 8085 with the `/api/v1/...` paths shown above.

## Quick Test Examples

Using curl:

```bash
curl http://localhost:8085/api/v1/selection/farms
```

```bash
curl -X POST http://localhost:8085/api/v1/tours/optimize-sequence \
  -H "Content-Type: application/json" \
  -d '{
    "depot":{"id":0,"name":"Depot A","latitude":7.8731,"longitude":80.7718},
    "farms":[
      {"id":1,"name":"Farm 1","latitude":7.8870,"longitude":80.7840},
      {"id":2,"name":"Farm 2","latitude":7.9001,"longitude":80.7605}
    ],
    "returnToDepot":true,
    "fuelConsumptionLitresPerKm":0.25
  }'
```

## Troubleshooting

### Service does not start

- Check that Java 17 is installed
- Ensure the SQLite database path is valid
- Confirm port 8085 is not already in use

### Frontend cannot reach backend

- Make sure the backend is running on port 8085
- Check that the frontend `NEXT_PUBLIC_TOUR_URL`/`NEXT_PUBLIC_SELECTION_URL` values match the backend endpoints
- Verify CORS is enabled for the frontend origin

## License

This project is distributed under the repository license included in the project root.
