package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class GroupRemote (
    val groupName: String,
    val groupSuffix: String,
    val unitName: String,
    val unitCourse: String,
)
