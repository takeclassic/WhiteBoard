package com.thinkers.whiteboard.data.common.exceptions

data class CredentialTypeNotExistException(override val message: String) : Exception(message)
data class LoginFailureException(override val message: String) : Exception(message)
data class UserCancelException(override val message: String? = null) : Exception(message)