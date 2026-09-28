package com.fieldvet.viewmodel

data class GuidanceStreamState(
    val text: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
)
