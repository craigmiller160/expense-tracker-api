package io.craigmiller160.expensetrackerapi.testutils

import com.github.javafaker.Faker
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class DataHelper(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
  private val faker = Faker()
  fun createTransaction(userId: Long, categoryId: TypedId<CategoryId>? = null): Transaction =
      transactionRepository.saveAndFlush(
          Transaction(
              userId = userId,
              expenseDate =
                  LocalDate.ofInstant(
                      faker.date().past(500, TimeUnit.DAYS).toInstant(), ZoneId.systemDefault()),
              description = faker.company().name(),
              amount = BigDecimal(faker.commerce().price()),
              categoryId = categoryId))

  fun createCategory(userId: Long, name: String): Category =
      categoryRepository.saveAndFlush(Category(userId = userId, name = name))

  fun createDefaultCategories(userId: Long): List<Category> =
      listOf(
          createCategory(userId, "Shopping"),
          createCategory(userId, "Restaurant"),
          createCategory(userId, "Entertainment"))
}
