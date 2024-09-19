# Jens Grassel Impure

A web service project built with Scala, focusing on traditional programming paradigms such as mutable state, side effects, and blocking operations.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Contributing](#contributing)
- [License](#license)

## Overview

Jens Grassel Impure is a Scala web application that focuses on traditional approaches to application development, contrasting functional programming. This project demonstrates the use of mutable state, side effects, and blocking I/O operations with JDBC and `Future` for asynchronous tasks.

The project highlights the challenges and practical use of side effects and mutability in web applications while still utilizing some modern Scala libraries.

## Features

- Traditional programming with mutable state and side effects
- Asynchronous operations using Scala's `Future`
- Blocking I/O operations
- JDBC for database interactions (PostgreSQL)
- Typesafe Config for configuration management
- Akka HTTP for building web services
- Flyway for database migrations

## Technologies Used

- **Scala 2.13**
- **Akka HTTP** for building HTTP services
- **Future** for asynchronous operations
- **JDBC** for database access
- **Typesafe Config** for configuration loading
- **Flyway** for database migration
- **PostgreSQL** as the relational database
- **SLF4J + Logback** for logging

## Getting Started

To run this project, you'll need to have [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/) installed.

### Prerequisites

- Scala 2.13.x
- SBT 1.9+
- PostgreSQL

### Clone the Repository

```bash
git clone https://github.com/your-username/jens-grassel-impure.git
cd jens-grassel-impure
```

### Install Dependencies

Run the following command to download the necessary dependencies:

```bash
sbt update
```

## Configuration

You can configure the application by modifying the `application.conf` file located in the `src/main/resources` directory.

The configuration includes settings for the HTTP server, database, and logging.

Example `application.conf`:

```hocon
api {
  host = "0.0.0.0"
  port = 8080
}

database {
  driver = "org.postgresql.Driver"
  url = "jdbc:postgresql://localhost:5432/impuredb"
  user = "dbuser"
  pass = "dbpass"
}
```

## Database Setup

Make sure you have PostgreSQL installed and running. Set up the database using the following commands:

```sql
CREATE DATABASE impuredb;
CREATE USER dbuser WITH PASSWORD 'dbpass';
GRANT ALL PRIVILEGES ON DATABASE impuredb TO dbuser;
```

## Running the Application

To run the application locally:

```bash
sbt run
```

The service will start on the host and port specified in `application.conf` (default: http://localhost:8080).

## Running Tests

To execute the unit tests:

```bash
sbt test
```

## API Endpoints

### Product Endpoints

- **GET /products** - List all products
- **POST /products** - Create a new product
- **GET /products/{id}** - Get details of a product by ID
- **PUT /products/{id}** - Update a product by ID

## Logging

Logging is configured using SLF4J and Logback. You can modify the logging behavior by editing the `logback.xml` file in `src/main/resources`.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please open an issue or submit a pull request.

To contribute:
1. Fork the repository
2. Create a new branch (e.g., `feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push the branch (`git push origin feature/new-feature`)
5. Open a pull request

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
