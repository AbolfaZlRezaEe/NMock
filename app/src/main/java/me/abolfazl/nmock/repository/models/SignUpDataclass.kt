package me.abolfazl.nmock.repository.models

data class SignUpDataclass(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val avatar: String? = null
)