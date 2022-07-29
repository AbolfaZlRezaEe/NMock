package me.abolfazl.nmock.repository.auth.models

data class SignUpDataclass(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val avatar: String? = null
)