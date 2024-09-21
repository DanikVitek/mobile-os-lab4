package me.danikvitek.lab4.service.dto

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val title: String,
    val artist: String,
)
