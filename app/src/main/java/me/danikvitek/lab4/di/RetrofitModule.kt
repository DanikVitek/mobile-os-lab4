package me.danikvitek.lab4.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import me.danikvitek.lab4.service.WebRadioService
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {
    private val webRadioService: WebRadioService by lazy {
        Retrofit.Builder()
            .baseUrl("https://webradio.io/api/")
            .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json; charset=UTF8")))
            .build()
            .create<WebRadioService>()
    }

    @Provides
    @Singleton
    fun provideWebRadioService(): WebRadioService = webRadioService
}