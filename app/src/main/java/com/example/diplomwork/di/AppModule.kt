package com.example.diplomwork.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.AuthApi
import com.example.diplomwork.data.api.FollowApi
import com.example.diplomwork.data.api.LocationApi
import com.example.diplomwork.data.api.NotificationApi
import com.example.diplomwork.data.api.PostApi
import com.example.diplomwork.data.api.ProfileApi
import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.api.UserApi
import com.example.diplomwork.data.interceptors.AuthInterceptor
import com.example.diplomwork.data.interceptors.CorsInterceptor
import com.example.diplomwork.data.interceptors.LoggingInterceptor
import com.example.diplomwork.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMutex(): Mutex {
        return Mutex()
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: LoggingInterceptor,
        authInterceptor: AuthInterceptor,
        corsInterceptor: CorsInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(corsInterceptor)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    fun provideSpotApi(retrofit: Retrofit): SpotApi = retrofit.create(SpotApi::class.java)

    @Provides
    fun providePostApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)

    @Provides
    fun provideFollowApi(retrofit: Retrofit): FollowApi = retrofit.create(FollowApi::class.java)

    @Provides
    fun provideLocationApi(retrofit: Retrofit): LocationApi =
        retrofit.create(LocationApi::class.java)

    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.10)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .error(android.R.drawable.ic_menu_report_image)
            .interceptorDispatcher(kotlinx.coroutines.Dispatchers.IO)
            .fetcherDispatcher(kotlinx.coroutines.Dispatchers.IO)
            .decoderDispatcher(kotlinx.coroutines.Dispatchers.IO)
            .transformationDispatcher(kotlinx.coroutines.Dispatchers.Default)
            .build()
    }

}