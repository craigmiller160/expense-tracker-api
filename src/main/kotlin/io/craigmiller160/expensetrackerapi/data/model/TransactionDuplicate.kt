package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionDuplicateId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableEntity
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "transaction_duplicates")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class TransactionDuplicate(
  val newTransactionId: UUID,
  val possibleDuplicateTransactionId: UUID,
  @Id override val id: TypedId<TransactionDuplicateId>,
  override val created: ZonedDateTime
) : ImmutableEntity<TransactionDuplicateId>
