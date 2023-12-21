package com.thinkers.whiteboard.data.database.pagingsource

interface DataSource {
    interface Holder<T> {
        fun create(type: T)
        fun getDataSource(): T
    }
}
