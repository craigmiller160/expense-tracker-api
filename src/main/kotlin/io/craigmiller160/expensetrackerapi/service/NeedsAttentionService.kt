package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionType
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class NeedsAttentionService {
  @org.springframework.transaction.annotation.Transactional
  fun getNeedsAttention(): TryEither<NeedsAttentionResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
      val needsAttentionCounts =
        transactionRepository.getAllNeedsAttentionCounts(userId).associateBy { it.type }
      val needsAttentionOldest =
        transactionRepository.getAllNeedsAttentionOldest(userId).associateBy { it.type }

      NeedsAttentionResponse(
        unconfirmed =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.UNCONFIRMED]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.UNCONFIRMED]?.date),
        uncategorized =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.UNCATEGORIZED]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.UNCATEGORIZED]?.date),
        duplicate =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.DUPLICATE]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.DUPLICATE]?.date),
        possibleRefund =
          CountAndOldest(
            count = needsAttentionCounts[NeedsAttentionType.POSSIBLE_REFUND]!!.count,
            oldest = needsAttentionOldest[NeedsAttentionType.POSSIBLE_REFUND]?.date))
    }
  }
}
