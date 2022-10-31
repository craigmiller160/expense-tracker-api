package io.craigmiller160.expensetrackerapi.testcore

import javax.transaction.Transactional
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@SpringBootTest
@ExtendWith(
  value =
    [
      TestContainerExtension::class,
      SpringExtension::class,
      MockExtension::class,
      OAuth2Extension::class])
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
annotation class ExpenseTrackerIntegrationTest()
