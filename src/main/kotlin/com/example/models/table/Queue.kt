package com.example.models.table

import kotlinx.serialization.Serializable


@Serializable
data class Queue (
    val id: Int,
    val queueName: String,
    val creatorToken: String,
    val group: Int,
    val description: String
)
