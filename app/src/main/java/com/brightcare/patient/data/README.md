# Chiropractor Data Integration

This module handles fetching chiropractor data from Firestore and displaying it in the UI.

## Architecture

```
Firestore Database → ChiropractorModel → ChiropractorRepository → ChiropractorViewModel → ChiroScreen → ChiropractorCard
```

## Data Flow

### 1. Firestore Structure
Based on `TrueSpine_Database_Info.json`, the chiropractors collection contains:
```json
{
  "chiropractors": {
    "documentId": {
      "role": "Doctor",
      "yearsOfExperience": 8,
      "name": "Dr. Joshua Miller",
      "contactNumber": "0918-987-6543",
      "specialization": "Pediatric & Family Care",
      "profileImageUrl": "",
      "email": "dr.joshuamiller09@gmail.com"
    }
  }
}
```

### 2. Data Model (`ChiropractorModel.kt`)
- Maps directly to Firestore document structure
- Generates dummy data for missing fields (rating, reviews, location, availability)
- Uses `@PropertyName` annotations for Firestore mapping

### 3. Repository (`ChiropractorRepository.kt`)
- Handles Firestore operations
- Provides methods for:
  - `getAllChiropractors()` - Fetch all chiropractors
  - `getChiropractorById(id)` - Fetch specific chiropractor
  - `searchBySpecialization(specialization)` - Search by specialty
  - `getAvailableChiropractors()` - Filter available only
- Transforms `ChiropractorModel` to `ChiropractorInfo` for UI

### 4. ViewModel (`ChiropractorViewModel.kt`)
- Manages UI state and data fetching
- Provides StateFlow for reactive UI updates
- Handles loading, error, and success states
- Implements search and filter functionality

### 5. UI Components
- `ChiroScreen` - Main screen that displays chiropractors list
- `ChiropractorCard` - Individual chiropractor card component
- `ChiroHeader` - Header with search functionality

## Dummy Data Generation

Since some fields are not present in Firestore, we generate realistic dummy data:

- **Rating**: 4.1-5.0 based on years of experience
- **Review Count**: 10-150 based on years of experience  
- **Location**: Random selection from major Philippine cities
- **Availability**: 80% chance of being available

## Usage

### In your screen:
```kotlin
@Composable
fun ChiroScreen(
    navController: NavController,
    viewModel: ChiropractorViewModel = hiltViewModel()
) {
    val chiropractors by viewModel.chiropractors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // UI implementation
}
```

### Repository methods:
```kotlin
// Get all chiropractors
val result = chiropractorRepository.getAllChiropractors()

// Search by specialization
val result = chiropractorRepository.searchBySpecialization("Pediatric & Family Care")

// Get available only
val result = chiropractorRepository.getAvailableChiropractors()
```

## Error Handling

The repository returns `Result<T>` objects that can be handled with:
```kotlin
result.onSuccess { data ->
    // Handle success
}.onFailure { exception ->
    // Handle error
}
```

## Dependencies

- Firebase Firestore
- Hilt for dependency injection
- Kotlin Coroutines for async operations
- Jetpack Compose for UI
- StateFlow for reactive state management


















