package io.craigmiller160.expensetrackerapi.data.constants

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import java.util.UUID

object CategoryConstants {
  val UNKNOWN_CATEGORY =
      Category(name = "Unknown", userId = TypedId(), color = "#3e442a").apply {
        uid = TypedId<CategoryId>(UUID.fromString("d908ec89-4a38-4f35-a1bf-d11ecd326e07"))
      }
}
