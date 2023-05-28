package io.craigmiller160.expensetrackerapi.function

import arrow.core.Either
import arrow.core.flatMap

typealias TryEither<T> = Either<Throwable, T>

fun <A, B> TryEither<A>.flatMapCatch(block: (A) -> B): TryEither<B> = flatMap { value ->
  Either.catch { block(value) }
}

fun <A> TryEither<A>.flatMapKeepRight(block: (A) -> TryEither<Any>): TryEither<A> =
    flatMap { value ->
      block(value).map { value }
    }
