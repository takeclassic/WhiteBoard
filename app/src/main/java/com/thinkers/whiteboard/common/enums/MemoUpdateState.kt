package com.thinkers.whiteboard.common.enums

sealed class MemoUpdateState {
    object NONE: MemoUpdateState()
    object INSERT: MemoUpdateState()
    object UPDATE: MemoUpdateState()
    object DELETE: MemoUpdateState()
}
