package com.thinkers.whiteboard.database.pagingsource

interface DataSource {
    interface Holder<T> {
        fun create(type: T)
        fun getDataSource(): T
    }
}
