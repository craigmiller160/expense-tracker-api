package io.craigmiller160.expensetrackerapi.common.crypto

import java.security.MessageDigest
import org.springframework.security.crypto.codec.Hex

object SHA256 {
  fun hash(value: String): String {
    val digest = MessageDigest.getInstance("sha256")
    return String(Hex.encode(digest.digest(value.toByteArray())))
  }
}
