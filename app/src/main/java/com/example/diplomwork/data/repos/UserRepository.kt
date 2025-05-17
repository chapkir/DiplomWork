package com.example.diplomwork.data.repos

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.AuthApi
import com.example.diplomwork.data.api.UserApi
import com.example.diplomwork.data.model.UserExistsResponse
import dagger.hilt.android.scopes.ActivityScoped
import retrofit2.Response
import javax.inject.Inject


@ActivityScoped
class UserRepository @Inject constructor(
    private val api: UserApi,
    private val sessionManager: SessionManager
) {
    suspend fun deleteAccount(): Response<Unit> {
        return api.deleteAccount()
    }
}