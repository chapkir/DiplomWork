package com.example.diplomwork.di

import com.example.diplomwork.network.ApiService
import com.example.diplomwork.network.ImageUploadService
import com.example.diplomwork.network.repos.AuthRepository
import com.example.diplomwork.network.repos.CommentRepository
import com.example.diplomwork.network.repos.ImageRepository
import com.example.diplomwork.network.repos.PictureRepository
import com.example.diplomwork.network.repos.ProfileRepository
import com.example.diplomwork.network.repos.SearchRepository
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
    fun provideImageRepository(imageUploadService: ImageUploadService) =
        ImageRepository(imageUploadService)

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService) = AuthRepository(apiService)

    @Provides
    @Singleton
    fun provideCommentRepository(apiService: ApiService) = CommentRepository(apiService)

    @Provides
    @Singleton
    fun provideProfileRepository(apiService: ApiService) = ProfileRepository(apiService)

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: ApiService) = SearchRepository(apiService)

    @Provides
    @Singleton
    fun providePictureRepository(apiService: ApiService) = PictureRepository(apiService)

}