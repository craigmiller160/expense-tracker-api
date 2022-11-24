package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableLegacyEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "transactions")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class Transaction(
  override val userId: Long,
  override val expenseDate: LocalDate,
  override val description: String,
  override val amount: BigDecimal,
  @Column(name = "content_hash", insertable = false, updatable = false)
  val contentHash: String = "",
  val markNotDuplicateNano: Long? = null,
  override val confirmed: Boolean = false,
  override val categoryId: TypedId<CategoryId>? = null,
  @Id override val id: TypedId<TransactionId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now(),
  override var updated: ZonedDateTime = ZonedDateTime.now(),
  @Version override val version: Long = 1
) : MutableLegacyEntity<TransactionId>, TransactionCommon
