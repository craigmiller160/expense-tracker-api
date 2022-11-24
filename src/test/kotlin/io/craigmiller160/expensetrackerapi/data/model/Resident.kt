package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "residents")
class Resident(var name: String = "") : MutableTableEntity<ResidentId>()
