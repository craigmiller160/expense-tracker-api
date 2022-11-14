package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class NeedsAttentionService {
  @Transactional fun getNeedsAttention(): TryEither<NeedsAttentionResponse> = TODO()
}
