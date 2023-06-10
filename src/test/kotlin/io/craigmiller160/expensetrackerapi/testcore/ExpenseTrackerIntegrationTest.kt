package io.craigmiller160.expensetrackerapi.testcore

import io.craigmiller160.testcontainers.common.TestcontainersExtension
import jakarta.transaction.Transactional
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@SpringBootTest
@ExtendWith(value = [TestcontainersExtension::class, SpringExtension::class, MockExtension::class])
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
annotation class ExpenseTrackerIntegrationTest()
