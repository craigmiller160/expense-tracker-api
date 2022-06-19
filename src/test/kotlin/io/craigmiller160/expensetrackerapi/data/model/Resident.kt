package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.AbstractMutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "residents")
data class Resident(val name: String) : AbstractMutableEntity<ResidentId>()
