# API Documentation

Base URL: `http://localhost:8080` (local) or via Kubernetes ingress.

Interactive documentation is also available at `/swagger-ui.html` when the service is running.

## Health Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/actuator/health` | Combined health status |
| GET | `/actuator/health/liveness` | Liveness probe (always UP) |
| GET | `/actuator/health/readiness` | Readiness probe (UP when DB is reachable) |
| GET | `/actuator/prometheus` | Prometheus metrics |

**GET /actuator/health response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

## Product Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

---

## POST /api/v1/products

**Request Body:**
```json
{
  "name": "Widget Pro",
  "description": "A professional widget",
  "price": 29.9900,
  "stock": 100
}
```

**Validation Rules:**
- `name`: required, not blank, max 255 chars
- `price`: required, ≥ 0
- `stock`: required, ≥ 0
- `description`: optional, max 1000 chars

**Response: 201 Created**
```json
{
  "id": 1,
  "name": "Widget Pro",
  "description": "A professional widget",
  "price": 29.9900,
  "stock": 100,
  "createdAt": "2026-06-14T10:00:00Z",
  "updatedAt": "2026-06-14T10:00:00Z"
}
```

**Error: 400 Bad Request (validation)**
```json
{
  "timestamp": "2026-06-14T10:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "messages": ["name: must not be blank"]
}
```

---

## GET /api/v1/products

**Query Parameters:**
| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Items per page |
| `sort` | string | `id,asc` | Sort field and direction |

**Response: 200 OK**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Widget Pro",
      "price": 29.9900,
      "stock": 100,
      "createdAt": "2026-06-14T10:00:00Z",
      "updatedAt": "2026-06-14T10:00:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

## GET /api/v1/products/{id}

**Path Parameters:**
- `id`: Long (auto-incremented)

**Response: 200 OK** — single ProductResponse

**Error: 404 Not Found**
```json
{
  "timestamp": "2026-06-14T10:00:00Z",
  "status": 404,
  "error": "Product not found with id: 999"
}
```

---

## PUT /api/v1/products/{id}

Full update — all fields must be provided.

**Request Body:** same as POST

**Response: 200 OK** — updated ProductResponse

---

## DELETE /api/v1/products/{id}

**Response: 204 No Content**

---

## Quick Test with curl

```bash
# Create
curl -s -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"desc","price":9.99,"stock":10}' | jq

# List
curl -s "http://localhost:8080/api/v1/products?page=0&size=5" | jq

# Get by ID
curl -s http://localhost:8080/api/v1/products/1 | jq

# Update
curl -s -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated","description":"new desc","price":19.99,"stock":5}' | jq

# Delete
curl -s -X DELETE http://localhost:8080/api/v1/products/1 -w "%{http_code}"
```
