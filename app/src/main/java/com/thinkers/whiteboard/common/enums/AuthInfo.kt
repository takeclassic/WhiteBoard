package com.thinkers.whiteboard.common.enums

import com.google.firebase.auth.AuthResult

sealed interface AuthInfo<out T> {
    data class Success<out T>(val authResult: T) : AuthInfo<T>
    data class Failure(val errorCode: AuthErrorCodes) : AuthInfo<Nothing>
}

enum class AuthErrorCodes(code: Int) {
    DEFAULT(1000),
    ALREADY_EXIST(1001),
    NOT_EXIST(1002),
    NETWORK(1003),
    NOT_VERIFIED(1004),
    NOT_EMAIL_FORM(1005),
    TOO_MANY_REQUEST(1006)
}

enum class AuthType {
    LOGIN, REGISTER
}