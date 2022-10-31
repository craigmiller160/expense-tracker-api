package io.craigmiller160.expensetrackerapi.testcore

import java.util.Collections
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.Mockito
import org.mockito.internal.util.MockUtil
import org.springframework.test.context.junit.jupiter.SpringExtension

class MockExtension : BeforeEachCallback, BeforeAllCallback {
  private val mockedBeans = Collections.synchronizedSet(mutableSetOf<Any>())

  override fun beforeEach(ctx: ExtensionContext) {
    synchronized(this.mockedBeans) { this.mockedBeans.forEach(Mockito::reset) }
  }

  override fun beforeAll(ctx: ExtensionContext) {
    synchronized(this.mockedBeans) {
      if (this.mockedBeans.isNotEmpty()) {
        return
      }

      val springContext = SpringExtension.getApplicationContext(ctx)
      val mockedBeans =
        springContext
          .getBeansOfType(Object::class.java)
          .values
          .filter { MockUtil.isMock(it) }
          .toSet()
      this.mockedBeans + mockedBeans
    }
  }
}
