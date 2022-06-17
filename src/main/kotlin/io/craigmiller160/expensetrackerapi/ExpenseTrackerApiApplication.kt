package io.craigmiller160.expensetrackerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ExpenseTrackerApiApplication

fun main(args: Array<String>) {
  runApplication<ExpenseTrackerApiApplication>(*args)
}
