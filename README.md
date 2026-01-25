# Feature Toggle Service

A Spring Boot-based feature toggle management system with attribute-based evaluation, client registration, and webhook notifications.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running Locally](#running-locally)
  - [Running with Different Profiles](#running-with-different-profiles)
- [API Documentation](#api-documentation)
  - [Authentication](#authentication)
  - [Attributes API](#attributes-api)
  - [Toggles API](#toggles-api)
  - [Evaluation API](#evaluation-api)
  - [Client Registration API](#client-registration-api)
- [Examples](#examples)
- [Database](#database)
- [Monitoring](#monitoring)
- [Error Handling](#error-handling)

## Overview

The Feature Toggle Service provides a centralized system for managing feature flags with attribute-based evaluation. It allows you to:

- Define attributes with specific data types (STRING, INTEGER, BOOLEAN)
- Create toggles linked to attributes with allow-list based evaluation
- Evaluate toggles based on runtime values
- Register clients for webhook notifications on toggle changes
- Audit all changes to toggles and attributes

## Features

- **Attribute Management**: Define typed attributes for evaluation
- **Toggle Management**: Create toggles with enable/disable state and allow-lists
- **Allow-List Evaluation**: Toggle evaluation based on whether values are in the allow-list
- **Client Registration**: Register webhook URLs to receive notifications on toggle changes
- **Audit Trail**: Complete audit log of all changes
- **API Key Security**: Simple API key-based authentication
- **Metrics**: Prometheus metrics for monitoring
- **Health Checks**: Spring Actuator endpoints for service health

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Data JPA**
- **H2 Database** (development)
- **PostgreSQL** (production)
- **Flyway** (database migrations)
- **Maven** (build tool)
- **Micrometer** (metrics)
- **Lombok** (code generation)

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL (for production profile)

### Running Locally

1. **Clone the repository**

```bash
git clone <repository-url>
cd feature-toggle
```

2. **Navigate to server directory**

```bash
cd server
```

3. **Run with Maven (dev profile)**

```bash
mvn spring-boot:run
```

The application will start on port 8080 with an H2 in-memory database.

4. **Build JAR**

```bash
mvn clean package
java -jar target/feature-toggle-0.0.1.jar
```

### Running with Different Profiles

#### Development Profile (default)

Uses H2 in-memory database with sample data:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Configuration:**
- Database: H2 in-memory
- API Key: `dev-local-key`
- Port: 8080

#### Production Profile

Uses PostgreSQL with environment variable configuration:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/feature_toggle
export DATABASE_USERNAME=feature_toggle
export DATABASE_PASSWORD=your-password
export API_KEY=your-secret-api-key

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Test Profile

Used for integration tests:

```bash
mvn test
```

## API Documentation

### Authentication

All API requests require an `X-API-Key` header:

```bash
curl -H "X-API-Key: dev-local-key" http://localhost:8080/api/toggles
```

### Attributes API

Attributes define the properties that toggles can evaluate against.

#### Create Attribute

```bash
POST /api/attributes
```

**Request Body:**
```json
{
  "name": "country",
  "description": "User's country code",
  "dataType": "STRING"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "country",
  "description": "User's country code",
  "dataType": "STRING",
  "createdAt": "2026-01-25T10:30:00",
  "updatedAt": "2026-01-25T10:30:00"
}
```

#### Get All Attributes

```bash
GET /api/attributes
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "country",
    "description": "User's country code",
    "dataType": "STRING",
    "createdAt": "2026-01-25T10:30:00",
    "updatedAt": "2026-01-25T10:30:00"
  }
]
```

#### Get Attribute by Name

```bash
GET /api/attributes/{name}
```

**Response:** `200 OK`

#### Update Attribute

```bash
PUT /api/attributes/{name}
```

**Request Body:**
```json
{
  "name": "country",
  "description": "Updated description",
  "dataType": "STRING"
}
```

**Response:** `200 OK`

#### Delete Attribute

```bash
DELETE /api/attributes/{name}
```

**Response:** `204 No Content`

### Toggles API

Toggles are feature flags linked to attributes with allow-list based evaluation.

#### Create Toggle

```bash
POST /api/toggles
```

**Request Body:**
```json
{
  "name": "new-checkout-flow",
  "description": "Enable new checkout experience",
  "enabled": true,
  "attributeName": "country",
  "allowListValues": ["US", "CA", "UK"]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "new-checkout-flow",
  "description": "Enable new checkout experience",
  "enabled": true,
  "attributeName": "country",
  "allowListValues": ["US", "CA", "UK"],
  "createdAt": "2026-01-25T10:35:00",
  "updatedAt": "2026-01-25T10:35:00"
}
```

#### Get All Toggles

```bash
GET /api/toggles
```

**Response:** `200 OK`

#### Get Toggle by Name

```bash
GET /api/toggles/{name}
```

**Response:** `200 OK`

#### Update Toggle

```bash
PUT /api/toggles/{name}
```

**Request Body:**
```json
{
  "name": "new-checkout-flow",
  "description": "Updated description",
  "enabled": false,
  "attributeName": "country",
  "allowListValues": ["US", "CA"]
}
```

**Response:** `200 OK`

#### Delete Toggle

```bash
DELETE /api/toggles/{name}
```

**Response:** `204 No Content`

#### Update Allow List

```bash
PUT /api/toggles/{name}/allow-list
```

**Request Body:**
```json
{
  "values": ["US", "CA", "UK", "AU"]
}
```

**Response:** `200 OK`

#### Get Toggle Clients

Get all registered clients for a specific toggle:

```bash
GET /api/toggles/{name}/clients
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "callbackUrl": "https://app.example.com/webhook",
    "toggleNames": ["new-checkout-flow"],
    "createdAt": "2026-01-25T10:40:00"
  }
]
```

### Evaluation API

Evaluate whether a toggle is enabled for a given attribute value.

#### Evaluate Toggle

```bash
GET /api/toggles/{name}/evaluate?value={attributeValue}
```

**Example:**
```bash
GET /api/toggles/new-checkout-flow/evaluate?value=US
```

**Response:** `200 OK`
```json
{
  "toggleName": "new-checkout-flow",
  "enabled": true,
  "value": "US",
  "reason": "Toggle is enabled and value is in allow list"
}
```

**Evaluation Logic:**
1. If toggle is disabled → `enabled: false`
2. If no value provided → `enabled: false`
3. If value not in allow-list → `enabled: false`
4. If toggle is enabled AND value in allow-list → `enabled: true`

**Possible Reasons:**
- `"Toggle is disabled"`
- `"No value provided for evaluation"`
- `"Value not in allow list"`
- `"Toggle is enabled and value is in allow list"`

### Client Registration API

Register webhook URLs to receive notifications when toggles change.

#### Register Client

```bash
POST /api/clients/register
```

**Request Body:**
```json
{
  "callbackUrl": "https://app.example.com/webhook",
  "toggleNames": ["new-checkout-flow", "beta-features"]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "callbackUrl": "https://app.example.com/webhook",
  "toggleNames": ["new-checkout-flow", "beta-features"],
  "createdAt": "2026-01-25T10:40:00"
}
```

#### Unregister Client

```bash
DELETE /api/clients/{id}
```

**Response:** `204 No Content`

#### Notification Payload

When a toggle changes, registered clients receive a `PUT` request to their callback URL:

```json
{
  "toggleName": "new-checkout-flow",
  "enabled": false,
  "allowListValues": ["US", "CA"]
}
```

## Examples

### Complete Workflow Example

```bash
# Set API key
API_KEY="dev-local-key"
BASE_URL="http://localhost:8080"

# 1. Create an attribute
curl -X POST "$BASE_URL/api/attributes" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "region",
    "description": "Geographic region",
    "dataType": "STRING"
  }'

# 2. Create a toggle
curl -X POST "$BASE_URL/api/toggles" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "premium-features",
    "description": "Enable premium features",
    "enabled": true,
    "attributeName": "region",
    "allowListValues": ["north-america", "europe"]
  }'

# 3. Evaluate the toggle
curl -X GET "$BASE_URL/api/toggles/premium-features/evaluate?value=north-america" \
  -H "X-API-Key: $API_KEY"

# 4. Register a client for notifications
curl -X POST "$BASE_URL/api/clients/register" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "callbackUrl": "https://myapp.com/webhook",
    "toggleNames": ["premium-features"]
  }'

# 5. Update the toggle (triggers notification)
curl -X PUT "$BASE_URL/api/toggles/premium-features" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "premium-features",
    "description": "Enable premium features",
    "enabled": false,
    "attributeName": "region",
    "allowListValues": ["north-america"]
  }'

# 6. Get all toggles
curl -X GET "$BASE_URL/api/toggles" \
  -H "X-API-Key: $API_KEY"
```

### Data Type Examples

#### STRING Attribute
```json
{
  "name": "country",
  "dataType": "STRING"
}
```
Allow list: `["US", "CA", "UK"]`

#### INTEGER Attribute
```json
{
  "name": "account_age_days",
  "dataType": "INTEGER"
}
```
Allow list: `["30", "60", "90"]`

#### BOOLEAN Attribute
```json
{
  "name": "is_premium",
  "dataType": "BOOLEAN"
}
```
Allow list: `["true"]`

## Database

### Development

Uses H2 in-memory database (auto-configured):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:feature_toggle_dev
    username: sa
    password: 
```

H2 Console available at: `http://localhost:8080/h2-console`

### Production

Requires PostgreSQL configuration via environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/feature_toggle
DATABASE_USERNAME=feature_toggle
DATABASE_PASSWORD=your-password
```

### Migrations

Database schema is managed by Flyway migrations in `src/main/resources/db/migration/`:

- `V1__initial_schema.sql` - Initial tables and indexes

To run migrations manually:

```bash
mvn flyway:migrate
```

### Schema

Main tables:
- `attributes` - Attribute definitions
- `toggles` - Toggle configurations
- `allow_list_entries` - Allow-list values for toggles
- `client_registrations` - Registered webhook clients
- `client_toggle_subscriptions` - Many-to-many relationship
- `audit_logs` - Audit trail of changes

## Monitoring

### Health Check

```bash
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Metrics

Prometheus metrics available at:

```bash
GET /actuator/prometheus
```

Custom metrics:
- `toggle_evaluation_duration_seconds` - Toggle evaluation latency
- `toggle_service_duration_seconds` - Toggle service operation latency
- `attribute_service_duration_seconds` - Attribute service operation latency

### Application Info

```bash
GET /actuator/info
```

## Error Handling

The API uses RFC 7807 Problem Details for HTTP APIs format for error responses.

### Error Response Format

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Toggle with name 'unknown-toggle' not found",
  "instance": "/api/toggles/unknown-toggle"
}
```

### Common HTTP Status Codes

- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Validation error or malformed request
- `401 Unauthorized` - Missing or invalid API key
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource (e.g., attribute/toggle name already exists)
- `500 Internal Server Error` - Server error

### Validation Errors

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Toggle name is required",
  "instance": "/api/toggles"
}
```

## Configuration Reference

### Application Properties

Key configuration properties in `application.yml`:

```yaml
spring:
  application:
    name: feature-toggle

security:
  api-key: ${API_KEY:dev-local-key}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Environment Variables

Production environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/feature_toggle` |
| `DATABASE_USERNAME` | Database username | `feature_toggle` |
| `DATABASE_PASSWORD` | Database password | `change-me` |
| `API_KEY` | API authentication key | `change-me` |

## Development

### Project Structure

```
server/
├── src/
│   ├── main/
│   │   ├── java/com/fnl33/featuretoggle/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── domain/          # JPA entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── repository/      # JPA repositories
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── application*.yml # Configuration files
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Test files
└── pom.xml                      # Maven configuration
```

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run tests only
mvn test
```

## License

This project is licensed under the terms specified in the repository.

## Support

For issues and questions, please open an issue in the repository.
