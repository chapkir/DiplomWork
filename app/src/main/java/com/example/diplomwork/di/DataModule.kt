package com.example.diplomwork.di

import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.repos.AuthRepository
import com.example.diplomwork.data.repos.CommentRepository
import com.example.diplomwork.data.repos.PictureRepository
import com.example.diplomwork.data.repos.ProfileRepository
import com.example.diplomwork.data.repos.SearchRepository
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.repos.FollowRepository
import com.example.diplomwork.data.repos.LocationRepository
import com.example.diplomwork.data.repos.UploadRepository
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
    fun provideUploadRepository(apiService: ApiService) = UploadRepository(apiService)

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, sessionManager: SessionManager) =
        AuthRepository(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideCommentRepository(apiService: ApiService) = CommentRepository(apiService)

    @Provides
    @Singleton
    fun provideProfileRepository(apiService: ApiService, sessionManager: SessionManager) =
        ProfileRepository(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideFollowRepository(apiService: ApiService) = FollowRepository(apiService)

    @Provides
    @Singleton
    fun provideLocationRepository(apiService: ApiService) = LocationRepository(apiService)

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: ApiService) = SearchRepository(apiService)

    @Provides
    @Singleton
    fun providePictureRepository(apiService: ApiService, sessionManager: SessionManager) =
        PictureRepository(apiService, sessionManager)

}