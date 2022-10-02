package io.craigmiller160.expensetrackerapi.common.crypto

import java.security.MessageDigest

object SHA256 {
  fun hash(value: String): ByteArray {
    val digest = MessageDigest.getInstance("sha256")
    return digest.digest(value.toByteArray())
  }
}
