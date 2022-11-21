package io.craigmiller160.expensetrackerapi.web.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions/rules/last-applied")
class LastRuleAppliedController {
  @GetMapping
  fun getLastAppliedRuleForTransaction() {
    TODO()
  }
}
