package com.brightcare.patient.di

import com.brightcare.patient.data.repository.ChiropractorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing chiropractor-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ChiropractorModule {
    
    @Provides
    @Singleton
    fun provideChiropractorRepository(): ChiropractorRepository {
        return ChiropractorRepository()
    }
}



