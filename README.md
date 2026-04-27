# Search Platform

A Spring Boot service that acts as a layer between consumers and Elasticsearch, providing JWT-based authentication and search capabilities.

## Prerequisites

- **Java 17** or later
- **Maven 3.6+**
- **Elasticsearch 8.x** running locally (default: `http://localhost:9200`)

## Project Structure

```
src/main/java/com/searchplatform/
├── SearchPlatformApplication.java    # Application entry point
├── config/
│   └── ElasticsearchConfig.java      # Elasticsearch client configuration
├── controller/
│   └── SearchController.java         # REST endpoints (/search/login, /search/query)
├── exception/
│   └── GlobalExceptionHandler.java   # Centralized error handling
├── model/
│   ├── LoginResponse.java            # Login response DTO
│   ├── Position.java                 # Position domain model
│   ├── SearchContext.java            # Search context with index list
│   ├── SearchRequest.java            # Search request DTO with validation
│   └── SearchResponse.java           # Search response DTO
└── service/
    ├── AuthenticationService.java    # User authentication (configurable users)
    ├── ElasticsearchService.java     # Elasticsearch query execution
    └── JwtService.java              # JWT token generation and validation
```

## Getting Started

### 1. Install and Start Elasticsearch

**Option A: Docker (recommended)**
```bash
docker run -d --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.13.0
```

**Option B: Download from [elastic.co](https://www.elastic.co/downloads/elasticsearch)**

Verify Elasticsearch is running:
```bash
curl http://localhost:9200
```

### 2. Create a Sample Index

```bash
# Create the "position" index with sample data
curl -X PUT "http://localhost:9200/position" -H "Content-Type: application/json" -d '{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "security": { "type": "text" },
      "qty": { "type": "keyword" },
      "unit_price": { "type": "keyword" }
    }
  }
}'

# Index sample documents
curl -X POST "http://localhost:9200/position/_doc/1" -H "Content-Type: application/json" -d '{
  "id": "1",
  "security": "AAPL",
  "qty": "100",
  "unit_price": "150.00"
}'

curl -X POST "http://localhost:9200/position/_doc/2" -H "Content-Type: application/json" -d '{
  "id": "2",
  "security": "GOOGL",
  "qty": "50",
  "unit_price": "2800.00"
}'
```

### 3. Build and Run the Service

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

The service starts on **port 8080** by default.

## API Usage

### 1. Login (Get JWT Token)

```bash
curl -X POST http://localhost:8080/search/login \
  -H "user: admin" \
  -H "password: admin123"
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

### 2. Search Query

```bash
curl -X POST http://localhost:8080/search/query \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<JWT_TOKEN_FROM_LOGIN>",
    "user": "admin",
    "query": "AAPL",
    "context": {
      "index": ["Position"]
    }
  }'
```

**Response:**
```json
{
  "position": [
    {
      "id": "1",
      "security": "AAPL",
      "qty": "100",
      "unit_price": "150.00"
    }
  ]
}
```

## Configuration

Configuration is managed via `src/main/resources/application.yml`:

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Application port |
| `spring.elasticsearch.uris` | `http://localhost:9200` | Elasticsearch connection URL |
| `search.jwt.secret` | (base64 encoded) | JWT signing secret |
| `search.jwt.expiration-ms` | `3600000` | JWT token expiration (1 hour) |
| `search.auth.users` | `admin:admin123, user1:password1` | Authorized users |

## Validation Rules

- **Query length:** minimum 3 characters, maximum 15 characters
- **Required fields:** `token`, `user`, `query`, and `context` must be provided
- **JWT validation:** token must be valid and not expired
- **User match:** the user in the token must match the user in the request body

## Running Tests

```bash
mvn test
```

The test suite includes:
- **JwtServiceTest** — token generation, validation, expiration, and username extraction
- **AuthenticationServiceTest** — credential validation, null handling, edge cases
- **ElasticsearchServiceTest** — search execution, empty results, error handling, multi-index search
- **SearchControllerTest** — endpoint integration tests for login and query (validation, auth, error handling)
