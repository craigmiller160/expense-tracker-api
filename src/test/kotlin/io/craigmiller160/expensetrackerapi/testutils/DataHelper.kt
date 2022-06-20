package io.craigmiller160.expensetrackerapi.testutils

import com.github.javafaker.Faker
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class DataHelper(private val transactionRepository: TransactionRepository) {
  private val faker = Faker()
  fun createTransaction(userId: Long): Transaction {
    val transaction =
        Transaction(
            userId = userId,
            expenseDate =
                LocalDate.ofInstant(
                    faker.date().past(500, TimeUnit.DAYS).toInstant(), ZoneId.systemDefault()),
            description = faker.company().name(),
            amount = BigDecimal(faker.commerce().price()))
    return transactionRepository.saveAndFlush(transaction)
  }
}
