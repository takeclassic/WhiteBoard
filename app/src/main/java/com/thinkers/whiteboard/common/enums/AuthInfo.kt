package com.thinkers.whiteboard.common.enums

import com.google.firebase.auth.AuthResult

sealed interface AuthInfo {
    data class Success(val authResult: AuthResult) : AuthInfo
    data class Failure(val errorCode: AuthErrorCodes) : AuthInfo
}

enum class AuthErrorCodes(code: Int) {
    ALREADY_EXIST(1001), NOT_EXIST(1002), NETWORK(1003), DEFAULT(1000)
}

enum class AuthType {
    LOGIN, REGISTER
}