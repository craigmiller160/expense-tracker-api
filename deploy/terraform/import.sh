#!/bin/sh

function import {
  terraform \
    import \
    -var="onepassword_token=$ONEPASSWORD_TOKEN"\
    "$1" "$2"
}

function plan {
  terraform plan \
    -var "onepassword_token=$ONEPASSWORD_TOKEN"
}

import "keycloak_openid_client.expense_tracker_api_dev" "apps-dev/c678d751-88da-4bb6-b90c-bc8410a0fcbf"
import "keycloak_openid_client.expense_tracker_api_prod" "apps-prod/ea64af43-0743-4d4d-aa91-f127cded6396"

import "keycloak_role.expense_tracker_api_access_role_dev" "apps-dev/ab7d0597-c76e-437a-ba38-96abc53b329c"
import "keycloak_role.expense_tracker_api_access_role_prod" "apps-prod/1d61598e-4f8d-4f8b-b8b8-3bb0bd1c78e5"

plan