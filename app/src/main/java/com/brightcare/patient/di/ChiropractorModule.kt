package com.brightcare.patient.di

import com.brightcare.patient.data.repository.ChiropractorRepository
import com.brightcare.patient.data.repository.ChiropractorProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
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
    
    @Provides
    @Singleton
    fun provideChiropractorProfileRepository(
        firestore: FirebaseFirestore
    ): ChiropractorProfileRepository {
        return ChiropractorProfileRepository(firestore)
    }
}



