data "keycloak_realm" "apps_dev" {
  realm = "apps-dev"
}

data "keycloak_realm" "apps_prod" {
  realm = "apps-prod"
}

locals {
  client_common = {
    client_id = "expense-tracker-api"
    name = "expense-tracker-api"
    enabled = true
    access_type = "BEARER-ONLY"
    service_accounts_enabled = true
  }
}

resource "keycloak_openid_client" "expense_tracker_api" {
  realm_id = data.keycloak_realm.apps_dev.id
  client_id = local.client_common.client_id
  name = local.client_common.name
  enabled = local.client_common.enabled
  access_type = local.client_common.access_type
  service_accounts_enabled = local.client_common.service_accounts_enabled
}