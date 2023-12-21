package com.thinkers.whiteboard.data.enums

sealed class MemoUpdateState {
    object NONE: MemoUpdateState()
    object INSERT: MemoUpdateState()
    object UPDATE: MemoUpdateState()
    object DELETE: MemoUpdateState()
}
