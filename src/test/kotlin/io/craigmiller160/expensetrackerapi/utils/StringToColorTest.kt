package io.craigmiller160.expensetrackerapi.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringToColorTest {
  @Test
  fun `converts a string to a random hex code color`() {
    val result = StringToColor.get("Hello World")
    assertEquals("", result)
  }
}
