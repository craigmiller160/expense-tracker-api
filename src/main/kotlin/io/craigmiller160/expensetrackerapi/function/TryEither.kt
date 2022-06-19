package io.craigmiller160.expensetrackerapi.function

import arrow.core.Either

typealias TryEither<T> = Either<Throwable, T>
