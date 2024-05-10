package com.example.models.table

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int,
    val login: String,
    val firstName: String,
    val lastName: String,
    val groupId: Int,
    val token: String
)
