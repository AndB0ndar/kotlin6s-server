package com.example.models.table

import kotlinx.serialization.Serializable


@Serializable
data class QueuePos(
    val id: Int,
    val queueId: Int,
    val userId: Int,
    val position: Int
)
