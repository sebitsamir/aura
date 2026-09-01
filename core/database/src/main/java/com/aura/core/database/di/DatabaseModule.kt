package com.aura.core.database.di

import android.content.Context
import androidx.room.Room
import com.aura.core.database.AuraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AuraDatabase = Room.databaseBuilder(
        context,
        AuraDatabase::class.java,
        "aura.db",
    ).build()
}
