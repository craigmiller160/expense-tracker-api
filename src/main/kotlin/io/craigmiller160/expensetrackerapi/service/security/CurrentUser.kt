package io.craigmiller160.expensetrackerapi.service.security

data class CurrentUser(
    val userId: Long,
    val userEmail: String,
    val firstName: String,
    val lastName: String,
    val roles: List<String>
)
