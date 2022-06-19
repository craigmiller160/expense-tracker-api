package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "residents")
data class Resident(
    val name: String,
    @Id override val id: TypedId<ResidentId> = TypedId(),
    override val created: ZonedDateTime = ZonedDateTime.now(),
    override var updated: ZonedDateTime = ZonedDateTime.now(),
    @Version override val version: Long = 1
) : MutableEntity<ResidentId>
