# Expense Tracker API

The backend for a program to help me track my expenses.

## How to Run (Dev)

Create a Postgres database `expense_tracker_dev`, if it isn't already.

Then use the command `./run.sh`.

## How to Run (Airplane Mode)

Use the `docker-compose.yml` file to start up a local postgres instance.

Then use the command `./run.sh airplane`.

NOTE: When in airplane mode, all security is disabled.

## How to Test (Fast Test Mode)

This app uses Testcontainers for its test suite. They can be slow to start. As an alternative, using the `docker-compose.yml` file to start those containers will stop Testcontainers from being used.

## Swagger

The swagger can be accessed locally via `https://localhost:8080/swagger-ui/index.html`

## Terraform

For the Terraform script to run, the following environment variables must be present on the machine. 

```
# The operator access token for communicating with 1Password
ONEPASSWORD_TOKEN=XXXXXXX
```