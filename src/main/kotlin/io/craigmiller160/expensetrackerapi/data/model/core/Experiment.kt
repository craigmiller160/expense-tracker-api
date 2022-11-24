package io.craigmiller160.expensetrackerapi.data.model.core

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.domain.Persistable

// TODO delete this
@Entity
@Table(name = "experiment")
class Experiment : Persistable<UUID> {
  @Id @set:JvmName("setId") var _id: UUID = TODO("initialize me")
  override fun getId(): UUID? = _id

  override fun isNew(): Boolean {
    TODO("Not yet implemented")
  }
}
