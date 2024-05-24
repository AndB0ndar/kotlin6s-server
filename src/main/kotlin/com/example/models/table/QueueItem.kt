package com.example.models.table

import kotlinx.serialization.Serializable


@Serializable
data class QueueItem(
    val id: Int,
    val queueId: Int,
    val userId: Int,
    val position: Int
)

@Serializable
data class QueueItemResponse(
    val queueName: String,
    val userLogin: String,
    val position: Int
)
