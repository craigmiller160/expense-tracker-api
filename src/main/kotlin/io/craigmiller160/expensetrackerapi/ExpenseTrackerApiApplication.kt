package io.craigmiller160.expensetrackerapi

import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.webutils.tls.TlsConfigurer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExpenseTrackerApiApplication(private val categoryRepository: CategoryRepository)

private const val TRUST_STORE_TYPE = "JKS"
private const val TRUST_STORE_PATH = "truststore.jks"
private const val TRUST_STORE_PASSWORD = "changeit"

fun main(args: Array<String>) {
  TlsConfigurer.configureTlsTrustStore(TRUST_STORE_PATH, TRUST_STORE_TYPE, TRUST_STORE_PASSWORD)
  runApplication<ExpenseTrackerApiApplication>(*args)
}
