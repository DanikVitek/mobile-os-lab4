package me.danikvitek.lab4.di

import android.content.Context
import androidx.room.withTransaction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.danikvitek.lab4.data.AppDatabase
import javax.inject.Singleton

class WithTransaction(private val func: suspend (block: suspend () -> Unit) -> Unit) {
    suspend operator fun invoke(block: suspend () -> Unit) = func(block)
}

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) = AppDatabase.getInstance(context)

    @Provides
    fun provideHistoryDao(appDatabase: AppDatabase) = appDatabase.historyDao()

    @Provides
    fun providesWithTransaction(appDatabase: AppDatabase) =
        WithTransaction(appDatabase::withTransaction)
}