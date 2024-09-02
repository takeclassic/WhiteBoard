package com.thinkers.whiteboard.data.common.types

sealed interface LoginTypes {
    object KaKaoLogin: LoginTypes
    object GoogleLogin: LoginTypes
}
