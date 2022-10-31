package io.craigmiller160.expensetrackerapi.testcore

import javax.transaction.Transactional
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@SpringBootTest
@ExtendWith(value = [SpringExtension::class])
@Transactional
@AutoConfigureMockMvc
annotation class ExpenseTrackerIntegrationTest()
