package com.brightcare.patient.di

import android.content.Context
import com.brightcare.patient.data.repository.PatientSignUpRepository
import com.brightcare.patient.data.repository.PatientLoginRepository
import com.brightcare.patient.data.repository.PatientForgotPasswordRepository
import com.brightcare.patient.data.repository.CompleteProfileRepository
import com.brightcare.patient.data.service.EmailService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for the application
 * Provides Firebase Auth and Repository instances
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provides Firebase Auth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    /**
     * Provides Firebase Firestore instance
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    /**
     * Provides Firebase Functions instance
     */
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }
    
    /**
     * Provides PatientSignUpRepository instance
     */
    @Provides
    @Singleton
    fun providePatientSignUpRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): PatientSignUpRepository {
        return PatientSignUpRepository(firebaseAuth, firestore, context)
    }
    
    /**
     * Provides PatientLoginRepository instance
     */
    @Provides
    @Singleton
    fun providePatientLoginRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): PatientLoginRepository {
        return PatientLoginRepository(firebaseAuth, firestore, context)
    }
    
    /**
     * Provides EmailService instance
     */
    @Provides
    @Singleton
    fun provideEmailService(
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context,
        firebaseFunctions: FirebaseFunctions
    ): EmailService {
        return EmailService(firebaseAuth, context, firebaseFunctions)
    }
    
    /**
     * Provides PatientForgotPasswordRepository instance
     */
    @Provides
    @Singleton
    fun providePatientForgotPasswordRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): PatientForgotPasswordRepository {
        return PatientForgotPasswordRepository(firebaseAuth, firestore, context)
    }
    
    /**
     * Provides CompleteProfileRepository instance
     */
    @Provides
    @Singleton
    fun provideCompleteProfileRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): CompleteProfileRepository {
        return CompleteProfileRepository(firebaseAuth, firestore, context)
    }
}









