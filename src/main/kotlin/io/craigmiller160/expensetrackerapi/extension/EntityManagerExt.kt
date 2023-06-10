package io.craigmiller160.expensetrackerapi.extension

import jakarta.persistence.EntityManager

fun <T> EntityManager.detachAndReturn(entity: T): T {
  detach(entity)
  return entity
}

fun EntityManager.flushAndClear() {
  flush()
  clear()
}
