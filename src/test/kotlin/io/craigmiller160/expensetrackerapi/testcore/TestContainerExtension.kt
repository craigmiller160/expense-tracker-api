package io.craigmiller160.expensetrackerapi.testcore

import arrow.core.getOrHandle
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.testcontainers.KeyCloakContainer
import io.craigmiller160.expensetrackerapi.testcontainers.PostgresContainer
import io.craigmiller160.expensetrackerapi.testutils.runCommand
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestContainerExtension : BeforeAllCallback {
  companion object {
    init {
      "docker ps"
        .runCommand()
        .map { getContainers(it) }
        .flatMapCatch { names ->
          if (!names.contains("expense_tracker_postgres")) {
            PostgresContainer.INSTANCE.start()
          }
          names
        }
        .flatMapCatch { names ->
          if (!names.contains("expense_tracker_keycloak")) {
            KeyCloakContainer.INSTANCE.start()
          }
          names
        }
        .getOrHandle { throw it }
    }

    private fun getContainers(output: String): List<String> =
      output
        .split("\n")
        .filter { row -> row.trim().isNotBlank() }
        .mapNotNull { row -> Regex("\\S+$").find(row)?.value }
  }
  override fun beforeAll(ctx: ExtensionContext) {}
}
