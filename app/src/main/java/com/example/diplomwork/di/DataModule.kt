package com.example.diplomwork.di

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.AuthApi
import com.example.diplomwork.data.api.FollowApi
import com.example.diplomwork.data.api.LocationApi
import com.example.diplomwork.data.api.NotificationApi
import com.example.diplomwork.data.api.PostApi
import com.example.diplomwork.data.api.ProfileApi
import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.api.UserApi
import com.example.diplomwork.data.repos.AuthRepository
import com.example.diplomwork.data.repos.FirebaseTokenRepository
import com.example.diplomwork.data.repos.FollowRepository
import com.example.diplomwork.data.repos.LocationRepository
import com.example.diplomwork.data.repos.NotificationRepository
import com.example.diplomwork.data.repos.PostRepository
import com.example.diplomwork.data.repos.ProfileRepository
import com.example.diplomwork.data.repos.SearchRepository
import com.example.diplomwork.data.repos.SpotRepository
import com.example.diplomwork.data.repos.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        fcmRepository: FirebaseTokenRepository,
        sessionManager: SessionManager
    ) =
        AuthRepository(api, fcmRepository, sessionManager)

    @Provides
    @Singleton
    fun provideProfileRepository(api: ProfileApi, sessionManager: SessionManager) =
        ProfileRepository(api, sessionManager)

    @Provides
    @Singleton
    fun provideFollowRepository(api: FollowApi) = FollowRepository(api)

    @Provides
    @Singleton
    fun provideLocationRepository(api: LocationApi) = LocationRepository(api)

    @Provides
    @Singleton
    fun provideSearchRepository(api: SpotApi) = SearchRepository(api)

    @Provides
    @Singleton
    fun provideSpotRepository(api: SpotApi, sessionManager: SessionManager) =
        SpotRepository(api, sessionManager)

    @Provides
    @Singleton
    fun providePostRepository(api: PostApi) = PostRepository(api)

    @Provides
    @Singleton
    fun provideNotificationRepository(api: NotificationApi) = NotificationRepository(api)

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi) = UserRepository(api)

    @Provides
    @Singleton
    fun provideFirebaseTokenRepository(api: NotificationApi) = FirebaseTokenRepository(api)

}