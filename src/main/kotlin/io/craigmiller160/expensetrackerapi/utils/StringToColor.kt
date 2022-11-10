package io.craigmiller160.expensetrackerapi.utils

object StringToColor {
  fun get(value: String): String = String.format("#%06x", 0xFFFFFF and value.hashCode())
}
