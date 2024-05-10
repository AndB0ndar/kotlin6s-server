package com.example.models.table

import kotlinx.serialization.Serializable


@Serializable
data class Group(
    val id: Int,
    val groupName: String,
    val groupSuffix: String,
    val unitName: String,
    val unitCourse: String
)
