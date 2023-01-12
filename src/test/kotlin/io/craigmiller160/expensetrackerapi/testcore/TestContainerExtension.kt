package io.craigmiller160.expensetrackerapi.testcore

import io.craigmiller160.expensetrackerapi.testcontainers.KeyCloakContainer
import io.craigmiller160.expensetrackerapi.testcontainers.PostgresContainer
import io.craigmiller160.expensetrackerapi.testutils.runCommand
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestContainerExtension : BeforeAllCallback {
  companion object {
    init {
      "docker ps".runCommand().map { getContainers(it) }.map { println(it) }

      TODO()

      PostgresContainer.INSTANCE.start()
      KeyCloakContainer.INSTANCE.start()
    }

    private fun getContainers(output: String): List<String> =
      output
        .split("\n")
        .filter { row -> row.trim().isNotBlank() }
        .mapNotNull { row -> Regex("\\S+$").find(row)?.value }
  }
  override fun beforeAll(ctx: ExtensionContext) {}
}
