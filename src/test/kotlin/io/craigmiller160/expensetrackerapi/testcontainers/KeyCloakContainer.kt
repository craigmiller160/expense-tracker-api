package io.craigmiller160.expensetrackerapi.testcontainers

import dasniko.testcontainers.keycloak.KeycloakContainer

class KeyCloakContainer : KeycloakContainer() {
  companion object {
    val INSTANCE = KeyCloakContainer()
  }
  init {
    withRealmImportFile("keycloak-realm.json")
  }
}
