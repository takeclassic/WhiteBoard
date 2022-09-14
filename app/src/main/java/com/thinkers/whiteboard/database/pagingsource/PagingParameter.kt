package com.thinkers.whiteboard.database.pagingsource

data class PagingParameter(
    val page: Int,
    val loadSize: Int,
    val noteName: String?
)