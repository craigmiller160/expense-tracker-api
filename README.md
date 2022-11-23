# Expense Tracker API

The backend for a program to help me track my expenses.

## How to Run (Dev)

Create a Postgres database `expense_tracker_dev`, if it isn't already.

Then use the command `./run.sh`.

## How to Run (Airplane Mode)

Use the `docker-compose.yml` file to start up a local postgres instance.

Then use the command `./run.sh airplane`.

NOTE: When in airplane mode, all security is disabled.

## Swagger

The swagger can be accessed locally via `https://localhost:8080/swagger-ui/index.html`