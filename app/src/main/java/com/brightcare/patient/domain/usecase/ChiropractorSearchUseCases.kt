package com.brightcare.patient.domain.usecase

import com.brightcare.patient.data.model.Chiropractor
import com.brightcare.patient.data.repository.ChiropractorSearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use cases for chiropractor search functionality
 * Use cases para sa chiropractor search functionality
 */

/**
 * Search chiropractors use case
 * Use case para sa paghahanap ng mga chiropractor
 */
class SearchChiropractorsUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(
        query: String,
        limit: Int = 20
    ): Result<List<Chiropractor>> {
        return repository.searchChiropractors(query.trim(), limit)
    }
}

/**
 * Get all active chiropractors use case
 * Use case para sa pagkuha ng lahat ng active na chiropractor
 */
class GetAllActiveChiropractorsUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(limit: Int = 50): Result<List<Chiropractor>> {
        return repository.getAllActiveChiropractors(limit)
    }
}

/**
 * Get chiropractors by specialization use case
 * Use case para sa pagkuha ng mga chiropractor ayon sa specialization
 */
class GetChiropractorsBySpecializationUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(
        specialization: String,
        limit: Int = 20
    ): Result<List<Chiropractor>> {
        return repository.getChiropractorsBySpecialization(specialization, limit)
    }
}

/**
 * Get chiropractor by ID use case
 * Use case para sa pagkuha ng chiropractor gamit ang ID
 */
class GetChiropractorByIdUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(chiropractorId: String): Result<Chiropractor?> {
        return repository.getChiropractorById(chiropractorId)
    }
}

/**
 * Get top rated chiropractors use case
 * Use case para sa pagkuha ng mga top rated na chiropractor
 */
class GetTopRatedChiropractorsUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Chiropractor>> {
        return repository.getTopRatedChiropractors(limit)
    }
}

/**
 * Get available specializations use case
 * Use case para sa pagkuha ng mga available na specialization
 */
class GetAvailableSpecializationsUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.getAvailableSpecializations()
    }
}

/**
 * Real-time search chiropractors use case
 * Use case para sa real-time search ng mga chiropractor
 */
class SearchChiropractorsFlowUseCase @Inject constructor(
    private val repository: ChiropractorSearchRepository
) {
    operator fun invoke(query: String): Flow<List<Chiropractor>> {
        return repository.searchChiropractorsFlow(query)
    }
}

/**
 * Combined chiropractor search use cases for easier injection
 * Pinagsama na chiropractor search use cases para sa mas madaling injection
 */
data class ChiropractorSearchUseCases(
    val searchChiropractors: SearchChiropractorsUseCase,
    val getAllActiveChiropractors: GetAllActiveChiropractorsUseCase,
    val getChiropractorsBySpecialization: GetChiropractorsBySpecializationUseCase,
    val getChiropractorById: GetChiropractorByIdUseCase,
    val getTopRatedChiropractors: GetTopRatedChiropractorsUseCase,
    val getAvailableSpecializations: GetAvailableSpecializationsUseCase,
    val searchChiropractorsFlow: SearchChiropractorsFlowUseCase
)















