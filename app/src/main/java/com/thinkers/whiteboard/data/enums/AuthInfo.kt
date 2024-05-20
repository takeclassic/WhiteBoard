package com.thinkers.whiteboard.data.enums

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

enum class AuthAddress(val str: String) {
    URL_VERIFY("https://whiteboard1.page.link/verify"),
    REDIRECT_PACKAGE_NAME("https://www.thinkers/whiteboard/verify")
}