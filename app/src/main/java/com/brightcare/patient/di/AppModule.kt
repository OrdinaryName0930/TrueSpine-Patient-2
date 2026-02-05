package com.brightcare.patient.di

import android.content.Context
import com.brightcare.patient.data.repository.PatientSignUpRepository
import com.brightcare.patient.data.repository.PatientLoginRepository
import com.brightcare.patient.data.repository.PatientForgotPasswordRepository
import com.brightcare.patient.data.repository.CompleteProfileRepository
import com.brightcare.patient.data.repository.EmergencyContactRepository
import com.brightcare.patient.data.repository.BookingRepository
import com.brightcare.patient.data.repository.NotificationRepository
import com.brightcare.patient.data.repository.ProfileValidationService
import com.brightcare.patient.data.repository.ReviewRepository
import com.brightcare.patient.data.service.EmailService
import com.brightcare.patient.data.service.AppointmentStatusMonitor
import com.brightcare.patient.data.service.LocalNotificationManager
import com.brightcare.patient.data.service.NotificationServiceManager
import com.brightcare.patient.data.service.SimpleAppointmentMonitor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
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
     * Provides Firebase Storage instance
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
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
        firebaseStorage: FirebaseStorage,
        @ApplicationContext context: Context
    ): CompleteProfileRepository {
        return CompleteProfileRepository(firebaseAuth, firestore, firebaseStorage, context)
    }
    
    /**
     * Provides EmergencyContactRepository instance
     */
    @Provides
    @Singleton
    fun provideEmergencyContactRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): EmergencyContactRepository {
        return EmergencyContactRepository(firebaseAuth, firestore)
    }
    
    /**
     * Provides ProfileValidationService instance
     */
    @Provides
    @Singleton
    fun provideProfileValidationService(
        completeProfileRepository: CompleteProfileRepository,
        emergencyContactRepository: EmergencyContactRepository
    ): ProfileValidationService {
        return ProfileValidationService(completeProfileRepository, emergencyContactRepository)
    }
    
    /**
     * Provides BookingRepository instance
     */
    @Provides
    @Singleton
    fun provideBookingRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        profileValidationService: ProfileValidationService,
        notificationRepository: NotificationRepository
    ): BookingRepository {
        return BookingRepository(firebaseAuth, firestore, profileValidationService, notificationRepository)
    }
    
    /**
     * Provides NotificationRepository instance
     */
    @Provides
    @Singleton
    fun provideNotificationRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): NotificationRepository {
        return NotificationRepository(firebaseAuth, firestore)
    }
    
    /**
     * Provides SimpleAppointmentMonitor instance
     */
    @Provides
    @Singleton
    fun provideSimpleAppointmentMonitor(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        notificationRepository: NotificationRepository
    ): SimpleAppointmentMonitor {
        return SimpleAppointmentMonitor(firebaseAuth, firestore, notificationRepository)
    }
    
    /**
     * Provides LocalNotificationManager instance
     */
    @Provides
    @Singleton
    fun provideLocalNotificationManager(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): LocalNotificationManager {
        return LocalNotificationManager(firebaseAuth, firestore)
    }
    
    /**
     * Provides AppointmentStatusMonitor instance
     */
    @Provides
    @Singleton
    fun provideAppointmentStatusMonitor(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        localNotificationManager: LocalNotificationManager
    ): AppointmentStatusMonitor {
        return AppointmentStatusMonitor(firebaseAuth, firestore, localNotificationManager)
    }
    
    /**
     * Provides NotificationServiceManager instance
     */
    @Provides
    @Singleton
    fun provideNotificationServiceManager(
        firebaseAuth: FirebaseAuth,
        appointmentStatusMonitor: AppointmentStatusMonitor,
        simpleAppointmentMonitor: SimpleAppointmentMonitor
    ): NotificationServiceManager {
        return NotificationServiceManager(firebaseAuth, appointmentStatusMonitor, simpleAppointmentMonitor)
    }
    
    /**
     * Provides ReviewRepository instance
     * Para sa paghawak ng mga review ng chiropractor
     */
    @Provides
    @Singleton
    fun provideReviewRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): ReviewRepository {
        return ReviewRepository(firestore, firebaseAuth)
    }
}









