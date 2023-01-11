package io.craigmiller160.expensetrackerapi.testcore

// import io.craigmiller160.expensetrackerapi.testcontainers.KeyCloakContainer
import io.craigmiller160.expensetrackerapi.testcontainers.PostgresContainer
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestContainerExtension : BeforeAllCallback {
  companion object {
    init {
      PostgresContainer.INSTANCE.start()
      //      KeyCloakContainer.INSTANCE.start()
    }
  }
  override fun beforeAll(ctx: ExtensionContext) {}
}
