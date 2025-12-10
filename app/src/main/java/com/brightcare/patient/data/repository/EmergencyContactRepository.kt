package com.brightcare.patient.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.brightcare.patient.data.model.EmergencyContact
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling emergency contact operations
 * Repository para sa pag-handle ng emergency contact operations
 */
@Singleton
class EmergencyContactRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "EmergencyContactRepository"
        private const val COLLECTION_CLIENTS = "client"
        private const val SUBCOLLECTION_EMERGENCY_CONTACTS = "emergency_contact"
        private const val MAX_EMERGENCY_CONTACTS = 3
    }
    
    /**
     * Get all emergency contacts for the current user
     * Kunin lahat ng emergency contacts para sa current user
     */
    suspend fun getEmergencyContacts(): Result<List<EmergencyContact>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Fetching emergency contacts for user: ${currentUser.uid}")
            
            val querySnapshot = firestore
                .collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                .get()
                .await()
            
            val contacts = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        EmergencyContact.fromMap(data).copy(id = document.id)
                    } else null
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing emergency contact document: ${document.id}", e)
                    null
                }
            }.sortedWith(compareByDescending<EmergencyContact> { it.isPrimary }.thenBy { it.createdAt })
            
            Log.d(TAG, "Successfully fetched ${contacts.size} emergency contacts")
            Result.success(contacts)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emergency contacts", e)
            // If it's a permission or index error, return empty list instead of failing
            if (e.message?.contains("FAILED_PRECONDITION") == true || 
                e.message?.contains("index") == true ||
                e.message?.contains("permission") == true) {
                Log.w(TAG, "Returning empty list due to Firestore configuration issue")
                Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to fetch emergency contacts: ${e.message}"))
            }
        }
    }
    
    /**
     * Add new emergency contact
     * Magdagdag ng bagong emergency contact
     */
    suspend fun addEmergencyContact(contact: EmergencyContact): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            // Check if user already has maximum contacts
            val existingContacts = getEmergencyContacts()
            if (existingContacts.isSuccess && existingContacts.getOrNull()?.size ?: 0 >= MAX_EMERGENCY_CONTACTS) {
                return Result.failure(Exception("Maximum of $MAX_EMERGENCY_CONTACTS emergency contacts allowed"))
            }
            
            val contactId = UUID.randomUUID().toString()
            val contactData = contact.copy(
                id = contactId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Adding emergency contact for user: ${currentUser.uid}")
            
            // If this contact is being set as primary, remove primary status from others first
            if (contactData.isPrimary) {
                val batch = firestore.batch()
                val contactsRef = firestore
                    .collection(COLLECTION_CLIENTS)
                    .document(currentUser.uid)
                    .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                
                // Remove primary status from all existing contacts
                val existingContacts = contactsRef.get().await()
                for (document in existingContacts.documents) {
                    batch.update(document.reference, "isPrimary", false, "updatedAt", System.currentTimeMillis())
                }
                
                // Add the new contact
                batch.set(contactsRef.document(contactId), contactData.toMap())
                
                batch.commit().await()
            } else {
                // Just add the contact normally
                firestore
                    .collection(COLLECTION_CLIENTS)
                    .document(currentUser.uid)
                    .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                    .document(contactId)
                    .set(contactData.toMap())
                    .await()
            }
            
            Log.d(TAG, "Emergency contact added successfully with ID: $contactId")
            Result.success(contactId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding emergency contact", e)
            Result.failure(Exception("Failed to add emergency contact: ${e.message}"))
        }
    }
    
    /**
     * Update existing emergency contact
     * I-update ang existing emergency contact
     */
    suspend fun updateEmergencyContact(contact: EmergencyContact): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            if (contact.id.isBlank()) {
                return Result.failure(Exception("Contact ID is required for update"))
            }
            
            val updatedContact = contact.copy(updatedAt = System.currentTimeMillis())
            
            Log.d(TAG, "Updating emergency contact: ${contact.id}")
            
            // If this contact is being set as primary, remove primary status from others first
            if (updatedContact.isPrimary) {
                val batch = firestore.batch()
                val contactsRef = firestore
                    .collection(COLLECTION_CLIENTS)
                    .document(currentUser.uid)
                    .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                
                // Remove primary status from all other contacts
                val existingContacts = contactsRef.get().await()
                for (document in existingContacts.documents) {
                    if (document.id != contact.id) {
                        batch.update(document.reference, "isPrimary", false, "updatedAt", System.currentTimeMillis())
                    }
                }
                
                // Update the current contact
                batch.set(contactsRef.document(contact.id), updatedContact.toMap())
                
                batch.commit().await()
            } else {
                // Just update the contact normally
                firestore
                    .collection(COLLECTION_CLIENTS)
                    .document(currentUser.uid)
                    .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                    .document(contact.id)
                    .set(updatedContact.toMap())
                    .await()
            }
            
            Log.d(TAG, "Emergency contact updated successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating emergency contact", e)
            Result.failure(Exception("Failed to update emergency contact: ${e.message}"))
        }
    }
    
    /**
     * Delete emergency contact
     * Tanggalin ang emergency contact
     */
    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            if (contactId.isBlank()) {
                return Result.failure(Exception("Contact ID is required for deletion"))
            }
            
            Log.d(TAG, "Deleting emergency contact: $contactId")
            
            firestore
                .collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
                .document(contactId)
                .delete()
                .await()
            
            Log.d(TAG, "Emergency contact deleted successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting emergency contact", e)
            Result.failure(Exception("Failed to delete emergency contact: ${e.message}"))
        }
    }
    
    /**
     * Set primary emergency contact
     * Itakda ang primary emergency contact
     */
    suspend fun setPrimaryContact(contactId: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Setting primary emergency contact: $contactId")
            
            val batch = firestore.batch()
            val contactsRef = firestore
                .collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_EMERGENCY_CONTACTS)
            
            // First, remove primary status from all contacts
            val allContacts = contactsRef.get().await()
            for (document in allContacts.documents) {
                batch.update(document.reference, "isPrimary", false, "updatedAt", System.currentTimeMillis())
            }
            
            // Then set the selected contact as primary
            batch.update(contactsRef.document(contactId), "isPrimary", true, "updatedAt", System.currentTimeMillis())
            
            batch.commit().await()
            
            Log.d(TAG, "Primary emergency contact set successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting primary emergency contact", e)
            Result.failure(Exception("Failed to set primary emergency contact: ${e.message}"))
        }
    }
    
    /**
     * Get the maximum number of allowed emergency contacts
     * Kunin ang maximum number ng allowed emergency contacts
     */
    fun getMaxContactsAllowed(): Int = MAX_EMERGENCY_CONTACTS
}
