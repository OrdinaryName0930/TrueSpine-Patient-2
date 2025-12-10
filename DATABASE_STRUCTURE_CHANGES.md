# Database Structure Changes

## Overview / Pangkalahatang-ideya

This document outlines the changes made to implement the new nested Firestore database structure for client data.

Ang dokumentong ito ay naglalaman ng mga pagbabagong ginawa para sa bagong nested Firestore database structure para sa client data.

## New Database Structure / Bagong Database Structure

### Before (Dati):
```
client
   └── {clientId} (Document with all fields)
```

### After (Ngayon):
```
client
   └── {clientId}                ← Document
         └── personal_data       ← Subcollection
                └── info         ← Document ID
                      (fields)   ← All user profile fields
```

## Files Modified / Mga Na-modify na Files

### 1. CompleteProfileRepository.kt
- **Location**: `app/src/main/java/com/brightcare/patient/data/repository/CompleteProfileRepository.kt`
- **Changes**:
  - Added constants for subcollection and document names
  - Modified `saveCompleteProfile()` to save data in nested structure
  - Modified `isProfileCompleted()` to check profile completion in nested structure
  - Modified `getProfileData()` to retrieve data from nested structure
  - Modified `updateProfile()` to update data in nested structure

### 2. PatientSignUpRepository (patient-signup.kt)
- **Location**: `app/src/main/java/com/brightcare/patient/data/repository/patient-signup.kt`
- **Changes**:
  - Modified email/password signup to save initial data in nested structure
  - Modified Google signup to save initial data in nested structure
  - Modified Facebook signup to save initial data in nested structure

### 3. PatientLoginRepository (patient-login.kt)
- **Location**: `app/src/main/java/com/brightcare/patient/data/repository/patient-login.kt`
- **Changes**:
  - Added constants for subcollection and document names
  - Modified `checkProfileCompletion()` to check profile status in nested structure
  - Modified `checkProfileCompletionAsync()` to check profile status in nested structure
  - Modified Google login to save new user data in nested structure
  - Modified Facebook login to save new user data in nested structure

### 4. PatientForgotPasswordRepository (patient-forgot-password.kt)
- **Location**: `app/src/main/java/com/brightcare/patient/data/repository/patient-forgot-password.kt`
- **Changes**:
  - Added constants for subcollection and document names
  - Modified `checkUserExists()` to use both Firebase Auth and Firestore collection group queries
  - Implemented collection group query to search across all `personal_data/info` documents
  - Added dual validation: Firebase Auth for account existence + Firestore for profile data
  - Handles email case sensitivity (lowercase and original case)
  - Provides fallback logic for data inconsistency scenarios

## Data Structure Details / Detalye ng Data Structure

### Firestore Path:
```
/client/{userId}/personal_data/info
```

### Fields Stored in 'info' Document:
- `firstName` - User's first name
- `lastName` - User's last name
- `suffix` - Name suffix (optional)
- `birthDate` - Date of birth
- `sex` - Gender
- `phoneNumber` - Contact number
- `country` - Country (default: "Philippines")
- `province` - Province
- `municipality` - Municipality/City
- `barangay` - Barangay
- `additionalAddress` - Additional address info
- `agreedToTerms` - Terms and conditions agreement
- `agreedToPrivacy` - Privacy policy agreement
- `profileCompleted` - Boolean flag indicating if profile is complete
- `email` - User's email address
- `deviceId` - Device identifier
- `createdAt` - Timestamp when record was created
- `updatedAt` - Timestamp when record was last updated

## Technical Implementation Details / Mga Teknikal na Detalye

### Forgot Password Email Validation / Pag-validate ng Email sa Forgot Password

With the new nested structure, checking if an email exists requires a special approach:

**Collection Group Query Approach:**
```kotlin
// Query across all 'info' documents in any client's personal_data subcollection
firestore.collectionGroup("info")
    .whereEqualTo("email", email.lowercase())
    .limit(1)
    .get()
```

**Dual Validation Strategy:**
1. **Firebase Auth Check** - Verifies if user account exists
2. **Firestore Collection Group Query** - Confirms profile data exists in nested structure
3. **Fallback Logic** - Handles data inconsistency gracefully

This ensures that password reset emails are only sent to valid, registered users while working with the new nested database structure.

## Benefits / Mga Benepisyo

1. **Better Organization**: Client data is now properly organized in subcollections
2. **Scalability**: Easier to add more subcollections for different data types
3. **Security**: More granular security rules can be applied to subcollections
4. **Performance**: Better query performance for specific data types
5. **Maintainability**: Cleaner code structure and easier to maintain
6. **Robust Email Validation**: Forgot password now validates against both Auth and Firestore

## Testing / Pagsusulit

The changes have been successfully compiled and tested:
- ✅ Build successful without errors
- ✅ No linting errors found
- ✅ All repository methods updated to use new structure

## Migration Notes / Mga Tala sa Migration

**Important**: Existing data in the old structure will need to be migrated to the new nested structure. Users with existing profiles may need to complete their profile again, or a data migration script should be implemented.

**Mahalaga**: Ang mga existing data sa lumang structure ay kailangang i-migrate sa bagong nested structure. Ang mga users na may existing profiles ay maaaring kailangang kumpletuhin ulit ang kanilang profile, o mag-implement ng data migration script.

## Implementation Status / Status ng Implementation

- ✅ CompleteProfileRepository updated
- ✅ PatientSignUpRepository updated  
- ✅ PatientLoginRepository updated
- ✅ PatientForgotPasswordRepository updated
- ✅ Build successful
- ✅ No compilation errors
- ✅ No linting errors

All changes have been successfully implemented and tested.
Lahat ng mga pagbabago ay matagumpay na na-implement at na-test.














