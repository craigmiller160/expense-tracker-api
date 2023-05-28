package io.craigmiller160.expensetrackerapi.testutils

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.function.TryEither

object ResourceUtils {
  fun getResourceBytes(path: String): TryEither<ByteArray> =
      javaClass.classLoader.getResourceAsStream(path)?.let { stream ->
        stream.use { Either.Right(it.readAllBytes()) }
      }
          ?: Either.Left(IllegalArgumentException("Unable to find resource at path: $path"))
}
