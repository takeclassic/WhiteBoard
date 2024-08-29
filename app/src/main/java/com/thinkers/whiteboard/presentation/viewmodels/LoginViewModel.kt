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
import com.kakao.sdk.user.UserApiClient
import com.thinkers.whiteboard.data.common.exceptions.CredentialTypeNotExistException
import com.thinkers.whiteboard.data.common.exceptions.LoginFailureException
import com.thinkers.whiteboard.data.common.types.LoginTypes
import com.thinkers.whiteboard.data.enums.AuthAddress
import com.thinkers.whiteboard.domain.SendVerifyEmailUseCase
import com.thinkers.whiteboard.domain.SignInUseCase
import com.thinkers.whiteboard.utils.safeResumeWith
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject


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
        const val LOGIN_TYPE_KAKAO = 0
        const val LOGIN_TYPE_GOOGLE = 1
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

//    suspend fun doLogin(): AuthInfo<AuthResult> =
//        suspendCancellableCoroutine { cont ->
//            val listener = OnCompleteListener {
//                if (it.isSuccessful) {
//                    FirebaseAuth.getInstance().currentUser?.let { user ->
//                        if (user.isEmailVerified) {
//                            cont.resume(AuthInfo.Success(it.result), null)
//                            return@OnCompleteListener
//                        }
//                    }
//                    cont.resume(AuthInfo.Failure(AuthErrorCodes.NOT_VERIFIED), null)
//                } else {
//                    when (it.exception!!) {
//                        is FirebaseAuthUserCollisionException -> {
//                            cont.resume(AuthInfo.Failure(AuthErrorCodes.ALREADY_EXIST), null)
//                        }
//                        is FirebaseAuthInvalidUserException -> {
//                            cont.resume(AuthInfo.Failure(AuthErrorCodes.NOT_EXIST), null)
//                        }
//                        is FirebaseAuthWebException -> {
//                            cont.resume(AuthInfo.Failure(AuthErrorCodes.NETWORK), null)
//                        }
//                        else -> {
//                            cont.resume(AuthInfo.Failure(AuthErrorCodes.DEFAULT), null)
//                        }
//                    }
//                }
//            }
//            signInUseCase(id, password, listener)
//        }

//    fun doLogin(context: Context) {
//        NaverIdLoginSDK.initialize(
//            context,
//            "l_9zzP0DgcqEgP0FQk29",
//            "fz6ibpWxhf",
//            "Whiteboard"
//        )
//
//        val oauthLoginCallback = object : OAuthLoginCallback {
//            override fun onSuccess() {
//                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
//                val accessToken = NaverIdLoginSDK.getAccessToken()
//                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
//                    override fun onSuccess(response: NidProfileResponse) {
//                        response.profile?.id
//                        Log.i(TAG, "id: ${response.profile?.id}, token: $accessToken")
//                    }
//                    override fun onFailure(httpStatus: Int, message: String) {
//                        Log.i(TAG, "failed2!")
//                    }
//                    override fun onError(errorCode: Int, message: String) {
//                        Log.i(TAG, "error2!")
//                    }
//                })
//            }
//            override fun onFailure(httpStatus: Int, message: String) {
//                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
//                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
//                Log.i(TAG, "failed1!")
//            }
//            override fun onError(errorCode: Int, message: String) {
//                onFailure(errorCode, message)
//                Log.i(TAG, "error1!")
//            }
//        }
//
//        NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
//    }

    suspend fun doLogin(loginType: LoginTypes, context: Context): Int {
        val loginResult = when (loginType) {
            is LoginTypes.KaKaoLogin -> loginKakao(context)
            is LoginTypes.GoogleLogin -> loginGoogle(context)
        }
        return loginResult
    }

    private suspend fun loginKakao(context: Context): Int {
        val loginResult = suspendCancellableCoroutine { cont ->
            val res = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
            Log.i(TAG, "is kakao available: $res")
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패", error)
                    cont.safeResumeWith(Result.failure(LoginFailureException("")))
                }
                else if (token != null) {
                    token.idToken
                    Log.i(TAG, "로그인 성공 ${token.accessToken}, id: ${token.idToken}")

                    val providerId = "oidc.kakao-provider"
                    val credential = OAuthProvider
                        .newCredentialBuilder(providerId)
                        .setIdToken(token.idToken!!)
                        .build()
                    Firebase.auth
                        .signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            Log.i(TAG, "파이어베이스 로그인 성공: ${authResult}")
                            cont.safeResumeWith(Result.success(200))
                        }
                        .addOnFailureListener { e ->
                            Log.i(TAG, "파이어베이스 로그인 실패: $e")
                            cont.safeResumeWith(Result.failure(e))
                        }
                }
            }
        }
        return loginResult
    }

    private suspend fun loginGoogle(context: Context): Int {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        val loginResult = suspendCancellableCoroutine { cont ->
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
                                cont.safeResumeWith(Result.failure(LoginFailureException("google login is failed! please try again later.")))
                            }
                    } else {
                        cont.safeResumeWith(Result.failure(CredentialTypeNotExistException("credential type is not google_id_token_credential")))
                    }
                }
                else -> {
                    cont.safeResumeWith(Result.failure(CredentialTypeNotExistException("credential is not CustomCredential")))
                }
            }
        }
        return loginResult
    }
}
