package com.example.diplomwork.di

import android.content.Context
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.network.ApiService
import com.example.diplomwork.network.ImageUploadService
import com.example.diplomwork.repos.AuthRepository
import com.example.diplomwork.repos.CommentRepository
import com.example.diplomwork.repos.PictureRepository
import com.example.diplomwork.repos.ProfileRepository
import com.example.diplomwork.repos.SearchRepository
import com.example.diplomwork.ui.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideImageUploadService(retrofit: Retrofit): ImageUploadService {
        return retrofit.create(ImageUploadService::class.java)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService) = AuthRepository(apiService)

    @Provides
    @Singleton
    fun provideCommentRepository(apiService: ApiService) = CommentRepository(apiService)

    @Provides
    @Singleton
    fun provideProfileRepository(apiService: ApiService): ProfileRepository = ProfileRepository(apiService)

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: ApiService) = SearchRepository(apiService)

    @Provides
    @Singleton
    fun providePictureRepository(apiService: ApiService) = PictureRepository(apiService)

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}