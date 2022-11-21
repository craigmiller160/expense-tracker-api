package io.craigmiller160.expensetrackerapi.extension

import javax.persistence.EntityManager

fun <T> EntityManager.detachAndReturn(entity: T): T {
  detach(entity)
  return entity
}
