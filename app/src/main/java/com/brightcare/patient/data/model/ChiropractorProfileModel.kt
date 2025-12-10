package com.brightcare.patient.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Comprehensive data model for chiropractor profile
 * Based on TrueSpine3.json Firestore structure
 * Modelo ng komprehensibong data para sa profile ng chiropractor
 */
data class ChiropractorProfileModel(
    @PropertyName("role")
    val role: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("firstName")
    val firstName: String = "",
    
    @PropertyName("lastName")
    val lastName: String = "",
    
    @PropertyName("middleName")
    val middleName: String = "",
    
    @PropertyName("suffix")
    val suffix: String = "",
    
    @PropertyName("specialization")
    val specialization: String = "",
    
    @PropertyName("prcLicenseNumber")
    val prcLicenseNumber: String = "",
    
    @PropertyName("contactNumber")
    val contactNumber: String = "",
    
    @PropertyName("about")
    val about: String = "",
    
    @PropertyName("yearsOfExperience")
    val yearsOfExperience: Int = 0,
    
    @PropertyName("startYear")
    val startYear: Int = 0,
    
    @PropertyName("profileImageUrl")
    val profileImageUrl: String = "",
    
    @PropertyName("pitahcAccreditationNumber")
    val pitahcAccreditationNumber: String = "",
    
    @PropertyName("email")
    val email: String = "",
    
    @PropertyName("serviceHours")
    val serviceHours: String = "",
    
    @PropertyName("education")
    val education: Map<String, EducationItem> = emptyMap(),
    
    @PropertyName("experienceHistory")
    val experienceHistory: Map<String, ExperienceItem> = emptyMap(),
    
    @PropertyName("professionalCredentials")
    val professionalCredentials: Map<String, ProfessionalCredentialItem> = emptyMap(),
    
    @PropertyName("others")
    val others: Map<String, OtherItem> = emptyMap()
)

/**
 * Education item data model
 * Modelo ng data para sa education item
 */
data class EducationItem(
    @PropertyName("id")
    val id: String? = null,
    
    @PropertyName("institution")
    val institution: String = "",
    
    @PropertyName("degree")
    val degree: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("startDate")
    val startDate: String = "",
    
    @PropertyName("endDate")
    val endDate: String = "",
    
    @PropertyName("current")
    val current: Boolean = false
)

/**
 * Experience item data model
 * Modelo ng data para sa experience item
 */
data class ExperienceItem(
    @PropertyName("id")
    val id: String? = null,
    
    @PropertyName("organization")
    val organization: String = "",
    
    @PropertyName("position")
    val position: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("startDate")
    val startDate: String = "",
    
    @PropertyName("endDate")
    val endDate: String = "",
    
    @PropertyName("current")
    val current: Boolean = false
)

/**
 * Professional credential item data model
 * Modelo ng data para sa professional credential item
 */
data class ProfessionalCredentialItem(
    @PropertyName("id")
    val id: String? = null,
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("institution")
    val institution: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("year")
    val year: String = "",
    
    @PropertyName("type")
    val type: String = "",
    
    @PropertyName("imageUrl")
    val imageUrl: String? = null
)

/**
 * Other item data model (achievements, awards, etc.)
 * Modelo ng data para sa iba pang item (mga achievement, award, etc.)
 */
data class OtherItem(
    @PropertyName("id")
    val id: String? = null,
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("category")
    val category: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("date")
    val date: String = ""
)







