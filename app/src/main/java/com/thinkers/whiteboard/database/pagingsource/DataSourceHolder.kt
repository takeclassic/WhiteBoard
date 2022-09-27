package com.thinkers.whiteboard.database.pagingsource

class DataSourceHolder<T>(): DataSource.Holder<T> {
    private var _dataSource: T? = null

    override fun create(typeClass: T) {
        _dataSource = typeClass
    }

    override fun getDataSource(): T {
       if (_dataSource != null) {
           return _dataSource as T
       }
        throw NullPointerException("dataSource is not created")
    }
}