package com.brightcare.patient.di

import android.content.Context
import com.brightcare.patient.utils.AuthenticationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for authentication dependencies
 * Hilt module para sa authentication dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {


    /**
     * Provides AuthenticationManager instance
     * Note: We need to provide this manually because the constructor needs @ApplicationContext
     */
    @Provides
    @Singleton
    fun provideAuthenticationManager(
        @ApplicationContext context: Context
    ): AuthenticationManager {
        return AuthenticationManager(context)
    }
}
