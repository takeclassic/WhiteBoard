package com.thinkers.whiteboard.presentation.viewmodels

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.thinkers.whiteboard.data.common.exceptions.CredentialTypeNotExistException
import com.thinkers.whiteboard.data.common.exceptions.LoginFailureException
import com.thinkers.whiteboard.data.common.exceptions.UserCancelException
import com.thinkers.whiteboard.data.common.types.LoginTypes
import com.thinkers.whiteboard.data.enums.AuthAddress
import com.thinkers.whiteboard.domain.SendVerifyEmailUseCase
import com.thinkers.whiteboard.domain.SignInUseCase
import com.thinkers.whiteboard.utils.safeResumeWith
import com.thinkers.whiteboard.utils.safeResumeWithException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val sendVerifyEmailUseCase: SendVerifyEmailUseCase
) : ViewModel() {
    companion object {
        // MARK: 비밆번호 패턴 - 영문대소문자, 특수문자, 숫자, 10~20자
        private val passwordRegex =
            Regex("""^(?=.*[a-zA-Z])(?=.*[!@#${'$'}%^*+=-])(?=.*[0-9]).{10,20}${'$'}""")
        private const val WEB_CLIENT_ID = "441355248374-gn98uuho9g4im7piamebpl38j3u91n16.apps.googleusercontent.com"

        const val TAG = "BackupViewModel"
    }

    var id: String = ""
    var password: String = ""
    var auth: FirebaseAuth? = null

    fun isPasswordCorrect(): Boolean {
        val matchResult = passwordRegex.matchEntire(password) ?: return false
        if (matchResult.value != password) {
            return false
        }
        return true
    }

    fun isEmailCorrect(): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        if (emailPattern.matcher(id).matches()) {
            return true
        }
        return false
    }

    fun sendVerifyEmail(callback: ((Exception?) -> Unit)? = null) {
        sendVerifyEmailUseCase(
            AuthAddress.URL_VERIFY.str,
            AuthAddress.REDIRECT_PACKAGE_NAME.str,
            callback
        )
    }

    suspend fun doLogin(loginType: LoginTypes, context: Context): Result<Int> {
        return runCatching {
            when (loginType) {
                is LoginTypes.KaKaoLogin -> loginKakao(context)
                is LoginTypes.GoogleLogin -> loginGoogle(context)
            }
        }
    }

    private suspend fun loginKakao(context: Context): Int {
        return suspendCancellableCoroutine { cont ->
            fun firebaseLogin(token: OAuthToken) {
                Log.i(TAG, "로그인 성공 ${token.accessToken}, id: ${token.idToken}")

                val providerId = "oidc.kakao-provider"
                val credential = OAuthProvider
                    .newCredentialBuilder(providerId)
                    .setIdToken(token.idToken!!)
                    .build()
                Firebase.auth
                    .signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        Log.i(TAG, "파이어베이스 로그인 성공: $authResult")
                        cont.safeResumeWith(Result.success(200))
                    }
                    .addOnFailureListener { e ->
                        Log.i(TAG, "파이어베이스 로그인 실패: $e")
                        cont.safeResumeWithException(e)
                    }
            }

            fun kakaoAccountLogin() {
                UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                    Log.i(TAG, "kakao web login result: $token, $error")
                    if (error != null) {
                        Log.e(TAG, "로그인 실패", error)
                        cont.safeResumeWithException(LoginFailureException(""))
                    } else if (token != null) {
                        firebaseLogin(token)
                    }
                }
            }

            val isKakaoAppAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
            Log.i(TAG, "is kakao available: $isKakaoAppAvailable")

            if (isKakaoAppAvailable) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    Log.i(TAG, "kakao app login result: $token, $error")
                    if (error != null) {
                        Log.e(TAG, "로그인 실패", error)

                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            cont.safeResumeWithException(UserCancelException())
                            return@loginWithKakaoTalk
                        }

                        kakaoAccountLogin()
                    } else if (token != null) {
                        firebaseLogin(token)
                    }
                }
            } else {
                kakaoAccountLogin()
            }
        }
    }

    private suspend fun loginGoogle(context: Context): Int {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val credentialManager = CredentialManager.create(context)
        val result =
            runCatching {
                credentialManager.getCredential(
                    request = request,
                    context = context,
                )
            }.onFailure {
                Log.i(TAG, "exception: $it")
            }.getOrElse { throw UserCancelException() }

        return suspendCancellableCoroutine { cont ->
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                        Firebase.auth
                            .signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { authResult ->
                                Log.i(TAG, "파이어베이스 로그인 성공: ${authResult}")
                                cont.safeResumeWith(Result.success(200))
                            }
                            .addOnFailureListener { e ->
                                Log.i(TAG, "파이어베이스 로그인 실패: $e")
                                cont.safeResumeWithException(LoginFailureException("google login is failed! please try again later."))
                            }
                    } else {
                        cont.safeResumeWithException(CredentialTypeNotExistException("credential type is not google_id_token_credential"))
                    }
                }
                else -> {
                    cont.safeResumeWithException(CredentialTypeNotExistException("credential is not CustomCredential"))
                }
            }
        }
    }
}
