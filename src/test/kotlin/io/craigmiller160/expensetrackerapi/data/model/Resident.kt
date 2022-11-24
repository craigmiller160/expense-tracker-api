package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "residents")
class Resident : MutableEntity<ResidentId>() {
  var name: String = ""
}
