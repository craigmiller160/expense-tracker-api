package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "transactions")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class Transaction(
  val userId: Long,
  val expenseDate: LocalDate,
  val description: String,
  val amount: BigDecimal,
  val contentHash: ByteArray,
  val confirmed: Boolean = false,
  val categoryId: TypedId<CategoryId>? = null,
  @Id override val id: TypedId<TransactionId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now(),
  override var updated: ZonedDateTime = ZonedDateTime.now(),
  @Version override val version: Long = 1
) : MutableEntity<TransactionId> {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Transaction

    if (userId != other.userId) return false
    if (expenseDate != other.expenseDate) return false
    if (description != other.description) return false
    if (amount != other.amount) return false
    if (!contentHash.contentEquals(other.contentHash)) return false
    if (confirmed != other.confirmed) return false
    if (categoryId != other.categoryId) return false
    if (id != other.id) return false
    if (created != other.created) return false
    if (updated != other.updated) return false
    if (version != other.version) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + expenseDate.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + amount.hashCode()
    result = 31 * result + contentHash.contentHashCode()
    result = 31 * result + confirmed.hashCode()
    result = 31 * result + (categoryId?.hashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + created.hashCode()
    result = 31 * result + updated.hashCode()
    result = 31 * result + version.hashCode()
    return result
  }
}
