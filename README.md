# Expense Tracker API

A REST API for tracking personal expenses, built with Spring Boot. Log expenses
with a description, amount, category and date; look them up by category or date
range; and get a spending summary with per-category totals.

Same stack and structure as the [Student Management REST API](https://github.com/Vansh13199/student-management-rest-api):
Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Bean Validation, and a
uniform error format.

## Tech stack

| Layer        | Technology                              |
|--------------|-----------------------------------------|
| Language     | Java 17                                 |
| Framework    | Spring Boot 3.3.5, Spring MVC           |
| Persistence  | Spring Data JPA, Hibernate, MySQL 8     |
| Validation   | Bean Validation (Hibernate Validator)   |
| Tests        | JUnit 5, Mockito, MockMvc, H2 (test)    |
| Build        | Maven                                   |

## Project structure

```
expense-tracker/
├── src/main/java/com/vansh/expensetracker/
│   ├── ExpenseTrackerApplication.java
│   ├── controller/ExpenseController.java
│   ├── service/ExpenseService.java
│   ├── repository/ExpenseRepository.java
│   ├── model/
│   │   ├── Expense.java          # JPA entity
│   │   ├── Category.java         # FOOD, TRANSPORT, ...
│   │   ├── ExpenseSummary.java   # summary response DTO
│   │   └── CategorySummary.java  # per-category totals DTO
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ExpenseNotFoundException.java
│       └── ErrorResponse.java
├── src/main/resources/application.properties
├── src/test/java/com/vansh/expensetracker/ExpenseControllerTest.java
├── db/init.sql                   # optional manual schema + sample data
└── postman/Expense_Tracker_API.postman_collection.json
```

## Data model

`Expense` (`expenses` table):

| Field         | Type         | Rules                                              |
|---------------|--------------|----------------------------------------------------|
| `id`          | Long         | Primary key, auto-generated                        |
| `description` | String       | Required, max 255 chars                            |
| `amount`      | BigDecimal   | Required, at least 0.01                            |
| `category`    | Category     | Required, one of the values below                  |
| `date`        | LocalDate    | Required, cannot be in the future (`yyyy-MM-dd`)   |

Categories: `FOOD`, `TRANSPORT`, `UTILITIES`, `ENTERTAINMENT`, `HEALTHCARE`,
`SHOPPING`, `EDUCATION`, `OTHER`. Category matching is case-insensitive, so
`food` and `FOOD` both work in request bodies and URLs.

## Setup

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8 running locally (default credentials `root` / `root`)

### Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. It creates the `expense_db`
database and the `expenses` table automatically on first run — no manual
schema step is needed. `db/init.sql` is there only if you want to create the
schema (and some sample rows) by hand instead.

### Configuration

Database settings come from environment variables, with local defaults:

| Variable      | Default                                                                          |
|---------------|----------------------------------------------------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/expense_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `root`                                                                             |
| `DB_PASSWORD` | `root`                                                                             |

```bash
# Example: different MySQL password
DB_PASSWORD=secret mvn spring-boot:run
```

### Run the tests

```bash
mvn clean test
```

Tests use an in-memory H2 database, so MySQL is not needed for them.

## API endpoints

Base URL: `http://localhost:8080/api/expenses`

| Method | Endpoint                          | Description                                  | Status   |
|--------|-----------------------------------|----------------------------------------------|----------|
| POST   | `/api/expenses`                   | Create an expense                            | 201      |
| GET    | `/api/expenses`                   | List all expenses                            | 200      |
| GET    | `/api/expenses/{id}`              | Get one expense                              | 200 / 404 |
| PUT    | `/api/expenses/{id}`              | Replace an expense                           | 200 / 404 |
| DELETE | `/api/expenses/{id}`              | Delete an expense                            | 204 / 404 |
| GET    | `/api/expenses/category/{category}` | Expenses in one category                   | 200 / 400 |
| GET    | `/api/expenses/date-range?start=&end=` | Expenses between two dates (inclusive) | 200 / 400 |
| GET    | `/api/expenses/summary`           | Total spend + per-category breakdown         | 200      |

## Examples

Create an expense:

```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"description":"Groceries at Big Bazaar","amount":49.99,"category":"FOOD","date":"2026-11-13"}'
```

```json
{
  "id": 1,
  "description": "Groceries at Big Bazaar",
  "amount": 49.99,
  "category": "FOOD",
  "date": "2026-11-13"
}
```

Get the spending summary:

```bash
curl http://localhost:8080/api/expenses/summary
```

```json
{
  "totalAmount": 170.49,
  "totalExpenses": 4,
  "byCategory": [
    { "category": "FOOD", "totalAmount": 49.99, "count": 1 },
    { "category": "TRANSPORT", "totalAmount": 20.00, "count": 1 },
    { "category": "UTILITIES", "totalAmount": 85.50, "count": 1 },
    { "category": "ENTERTAINMENT", "totalAmount": 15.00, "count": 1 }
  ]
}
```

List expenses in a date range:

```bash
curl "http://localhost:8080/api/expenses/date-range?start=2026-11-01&end=2026-11-30"
```

## Error format

Every failed request returns the same JSON shape:

```json
{
  "timestamp": "2026-11-13T19:45:00.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found with id: 99",
  "path": "/api/expenses/99",
  "fieldErrors": null
}
```

| Status | When                                                                     |
|--------|--------------------------------------------------------------------------|
| 400    | Validation failed (per-field details in `fieldErrors`), bad category, bad date, malformed JSON, non-numeric id |
| 404    | No expense with that id                                                |
| 500    | Unexpected server error (no stack trace is leaked)                     |

## Postman

Import `postman/Expense_Tracker_API.postman_collection.json`. It has one
request per endpoint, plus a 404 check. Run them in order: **Create Expense**
saves the new id to `{{expense_id}}`, which the Get/Update/Delete requests use.
`{{base_url}}` defaults to `http://localhost:8080`.

## License

MIT — see [LICENSE](LICENSE).
