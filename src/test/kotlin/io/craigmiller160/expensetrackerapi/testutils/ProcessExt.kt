package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.File
import java.util.concurrent.TimeUnit

fun String.runCommand(
  workingDir: File = File("."),
  timeoutAmount: Long = 60,
  timeoutUnit: TimeUnit = TimeUnit.SECONDS
): TryEither<String> {
  val processBuilder =
    ProcessBuilder("\\s".toRegex().split(this))
      .directory(workingDir)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
  return TryEither.catch {
    processBuilder
      .start()
      .also { it.waitFor(timeoutAmount, timeoutUnit) }
      .inputStream
      .bufferedReader()
      .readText()
  }
}
