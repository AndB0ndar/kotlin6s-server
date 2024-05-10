package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class QueueReceiveRemote (
    val queueName: String,
    val group: String,  // groupName
    val description: String,
)
