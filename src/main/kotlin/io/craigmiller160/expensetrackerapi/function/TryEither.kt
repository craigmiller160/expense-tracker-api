package io.craigmiller160.expensetrackerapi.function

import arrow.core.Either
import arrow.core.continuations.EagerEffectScope
import arrow.core.continuations.either

typealias TryEither<T> = Either<Throwable, T>

object tryEither {
  inline fun <T> eager(crossinline f: suspend EagerEffectScope<Throwable>.() -> T) = either.eager(f)
}
