package com.example.models

import kotlinx.serialization.Serializable


@Serializable
data class LoginReceiveRemote (
    val login: String,
    val password: String
)

@Serializable
data class RegisterReceiveRemote (
    val login: String,
    val firsName: String,
    val lastName: String,
    val group: String,
    val password: String
)

@Serializable
data class TokenResponseRemote (
    val token: String
)
