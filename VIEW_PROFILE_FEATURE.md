# View Profile Feature Documentation

## Overview / Pangkalahatang Paglalarawan

Ang View Profile feature ay nagbibigay ng komprehensibong pagpapakita ng chiropractor profile information na may organized tabs para sa iba't ibang kategorya ng impormasyon.

The View Profile feature provides a comprehensive display of chiropractor profile information with organized tabs for different categories of information.

## Features / Mga Feature

### 1. Tab-based Navigation
- **Overview Tab**: Basic information, contact details, at professional information
- **Education Tab**: Educational background at mga degree
- **Experience Tab**: Work experience at employment history
- **Credentials Tab**: Professional credentials, certificates, at awards
- **Others Tab**: Additional achievements at miscellaneous information

### 2. Comprehensive Profile Display
- Profile image na may circular design
- Complete name at specialization
- Years of experience at PRC license number
- Contact information (email, phone, service hours)
- Professional accreditation numbers

### 3. Book Now Functionality
- Prominent "Book Now" button sa profile header
- Direct navigation sa appointment booking screen
- Integration sa existing booking system

## Technical Implementation / Technical na Implementation

### Data Models

#### ChiropractorProfileModel
```kotlin
data class ChiropractorProfileModel(
    val role: String,
    val name: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val suffix: String,
    val specialization: String,
    val prcLicenseNumber: String,
    val contactNumber: String,
    val about: String,
    val yearsOfExperience: Int,
    val startYear: Int,
    val profileImageUrl: String,
    val pitahcAccreditationNumber: String,
    val email: String,
    val serviceHours: String,
    val education: Map<String, EducationItem>,
    val experienceHistory: Map<String, ExperienceItem>,
    val professionalCredentials: Map<String, ProfessionalCredentialItem>,
    val others: Map<String, OtherItem>
)
```

#### Supporting Data Models
- `EducationItem`: Para sa educational background
- `ExperienceItem`: Para sa work experience
- `ProfessionalCredentialItem`: Para sa credentials at certificates
- `OtherItem`: Para sa additional achievements

### Repository Layer

#### ChiropractorProfileRepository
- `getChiropractorProfile(chiropractorId: String)`: Fetch specific chiropractor profile
- `getAllChiropractorProfiles()`: Get all chiropractor profiles
- `searchChiropractors(query: String)`: Search functionality
- `getChiropractorsBySpecialization(specialization: String)`: Filter by specialization
- `getChiropractorsByExperience(minYears: Int)`: Filter by experience

### ViewModel Layer

#### ViewProfileViewModel
- Manages UI state para sa ViewProfileScreen
- Handles loading, error, at success states
- Provides data sa UI components
- Implements refresh functionality

### UI Components

#### ViewProfileScreen
- Main screen na may tab navigation
- Handles different UI states (loading, error, success)
- Integrates lahat ng tab components

#### ProfileTabComponents
- `EducationTab`: Displays educational background
- `ExperienceTab`: Shows work experience
- `CredentialsTab`: Lists professional credentials
- `OthersTab`: Additional achievements
- `EmptyStateCard`: For empty data states

## Navigation / Navigation

### Route Definition
```kotlin
const val VIEW_PROFILE = "view_profile/{chiropractorId}"
fun viewProfile(chiropractorId: String) = "view_profile/$chiropractorId"
```

### Navigation Usage
```kotlin
// From ChiropractorCard
navController.navigate(NavigationRoutes.viewProfile(chiropractor.id))

// From any screen
navController.navigate("view_profile/chiropractorId")
```

## Firestore Integration

### Data Structure
Ang feature ay sumusunod sa existing Firestore structure na nakita sa `TrueSpine3.json`:

```json
{
  "chiropractors": {
    "chiropractorId": {
      "role": "Doctor",
      "name": "Dr. Full Name",
      "firstName": "First",
      "lastName": "Last",
      "middleName": "Middle",
      "specialization": "Specialty",
      "prcLicenseNumber": "License Number",
      "contactNumber": "Phone",
      "about": "Description",
      "yearsOfExperience": 10,
      "startYear": 2014,
      "profileImageUrl": "https://...",
      "pitahcAccreditationNumber": "Accreditation",
      "email": "email@example.com",
      "serviceHours": "Schedule",
      "education": {
        "educationId": {
          "institution": "University",
          "degree": "Degree",
          "description": "Description",
          "startDate": "Start Date",
          "endDate": "End Date",
          "current": false
        }
      },
      "experienceHistory": {
        "experienceId": {
          "organization": "Company",
          "position": "Position",
          "description": "Description",
          "startDate": "Start Date",
          "endDate": "End Date",
          "current": false
        }
      },
      "professionalCredentials": {
        "credentialId": {
          "title": "Credential Title",
          "institution": "Institution",
          "description": "Description",
          "year": "Year",
          "type": "Type",
          "imageUrl": "https://..."
        }
      },
      "others": {
        "otherId": {
          "title": "Achievement Title",
          "category": "Category",
          "description": "Description",
          "date": "Date"
        }
      }
    }
  }
}
```

## Usage Examples / Mga Halimbawa ng Paggamit

### 1. Viewing Profile from Chiropractor List
```kotlin
ChiropractorCard(
    chiropractor = chiropractor,
    onViewProfileClick = {
        navController.navigate(NavigationRoutes.viewProfile(chiropractor.id))
    },
    onBookClick = {
        navController.navigate("book_appointment/${chiropractor.id}")
    }
)
```

### 2. Direct Profile Access
```kotlin
// Navigate directly to profile
navController.navigate("view_profile/specificChiropractorId")
```

## Error Handling / Error Handling

### Loading States
- Loading indicator habang nag-fetch ng data
- Error screen na may retry functionality
- Empty states para sa walang data

### Error Messages
- Network errors
- Data not found errors
- General error handling na may user-friendly messages

## Future Enhancements / Mga Future Enhancement

1. **Offline Support**: Cache profile data para sa offline viewing
2. **Favorite Profiles**: Mark profiles as favorites
3. **Profile Sharing**: Share profile information
4. **Reviews Integration**: Display patient reviews at ratings
5. **Appointment History**: Show past appointments na may specific chiropractor
6. **Real-time Updates**: Live updates sa profile information
7. **Search within Profile**: Search functionality sa loob ng profile tabs

## Dependencies / Mga Dependency

- Jetpack Compose para sa UI
- Hilt para sa dependency injection
- Firebase Firestore para sa data storage
- Coil para sa image loading
- Navigation Compose para sa navigation
- Lifecycle Compose para sa state management

## Testing / Testing

### Unit Tests
- Repository layer testing
- ViewModel testing
- Data model validation

### UI Tests
- Screen navigation testing
- Tab switching functionality
- Error state handling

## Accessibility / Accessibility

- Proper content descriptions para sa screen readers
- Keyboard navigation support
- High contrast support
- Text scaling support

---

**Note**: Ang feature na ito ay fully integrated sa existing BrightCare Patient app architecture at sumusunod sa established patterns at conventions ng project.














