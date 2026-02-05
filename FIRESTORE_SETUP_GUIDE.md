# Firestore Setup Guide for Chiropractor Collection

## Overview / Pangkalahatang Paglalarawan

Your app now uses a dedicated `chiropractor` collection in Firestore to display all chiropractors in the message screen. Here's everything you need to set up in Firebase Console.

## 🗂️ Required Firestore Collections / Mga Kinakailangang Collection

### 1. **`chiropractor` Collection** (Main chiropractor data)
```
chiropractor/{chiropractorId}
├── fullName: "Dr. John Smith"
├── email: "dr.smith@example.com"
├── profileImage: "https://firebasestorage.googleapis.com/..."
├── specialization: "Spine Specialist"
├── licenseNumber: "CHR12345"
├── phoneNumber: "+1234567890"
├── experience: 15
├── rating: 4.8
├── reviewCount: 120
├── isAvailable: true
├── bio: "Experienced chiropractor specializing in..."
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

### 2. **`users` Collection** (Patient data)
```
users/{patientId}
├── uid: "patient123"
├── fullName: "Jane Doe"
├── email: "jane@example.com"
├── profileImage: "https://..."
├── role: "patient"
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

### 3. **`conversations` Collection** (Chat metadata)
```
conversations/{conversationId}
├── id: "conv123"
├── participants: ["patient123", "chiropractor456"]
├── lastMessage: "Thank you for the treatment"
├── lastMessageType: "text"
├── lastMessageSenderId: "patient123"
├── updatedAt: Timestamp
├── createdAt: Timestamp
└── unreadCounts: {
    "patient123": 0,
    "chiropractor456": 2
}
```

### 4. **`conversations/{conversationId}/messages` Subcollection**
```
conversations/{conversationId}/messages/{messageId}
├── id: "msg123"
├── senderId: "patient123"
├── type: "text" | "image" | "file"
├── content: "Hello doctor"
├── fileUrl: "https://..." (for images/files)
├── fileName: "document.pdf" (for files)
├── fileSize: 1024000
├── mimeType: "application/pdf"
├── timestamp: Timestamp
├── isRead: false
└── replyToMessageId: "msg122" (optional)
```

## 🔧 Firebase Console Setup Steps / Mga Hakbang sa Firebase Console

### **Step 1: Create Chiropractor Collection**

1. Go to **Firebase Console** → **Firestore Database**
2. Click **"Start collection"**
3. Collection ID: `chiropractor`
4. Add your first chiropractor document:

```json
{
  "fullName": "Dr. John Smith",
  "email": "dr.smith@brightcare.com",
  "profileImage": "",
  "specialization": "Spine Specialist",
  "licenseNumber": "CHR001",
  "phoneNumber": "+1234567890",
  "experience": 15,
  "rating": 4.8,
  "reviewCount": 120,
  "isAvailable": true,
  "bio": "Experienced chiropractor specializing in spinal health and rehabilitation.",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### **Step 2: Add More Sample Chiropractors**

Add at least 3-5 chiropractors for testing:

```json
// Document ID: chiropractor_002
{
  "fullName": "Dr. Sarah Johnson",
  "email": "dr.johnson@brightcare.com",
  "profileImage": "",
  "specialization": "Sports Medicine",
  "licenseNumber": "CHR002",
  "phoneNumber": "+1234567891",
  "experience": 12,
  "rating": 4.9,
  "reviewCount": 95,
  "isAvailable": true,
  "bio": "Sports medicine specialist focusing on athletic injuries and performance.",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

```json
// Document ID: chiropractor_003
{
  "fullName": "Dr. Michael Brown",
  "email": "dr.brown@brightcare.com",
  "profileImage": "",
  "specialization": "Pediatric Chiropractic",
  "licenseNumber": "CHR003",
  "phoneNumber": "+1234567892",
  "experience": 8,
  "rating": 4.7,
  "reviewCount": 78,
  "isAvailable": false,
  "bio": "Pediatric chiropractor specializing in children's spinal health.",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### **Step 3: Set Up Security Rules**

Go to **Firestore** → **Rules** and update:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    function isParticipant(participantIds) {
      return request.auth.uid in participantIds;
    }
    
    function isPatient() {
      return isAuthenticated() && 
        exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "patient";
    }
    
    function isChiropractor() {
      return isAuthenticated() && 
        exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "chiropractor";
    }
    
    // Users collection
    match /users/{userId} {
      allow read, write: if isAuthenticated() && isOwner(userId);
      allow read: if isAuthenticated() && 
        isPatient() && 
        resource.data.role == "chiropractor";
      allow read: if isAuthenticated() && 
        isChiropractor() && 
        resource.data.role == "patient";
    }
    
    // Chiropractor collection - patients can read all chiropractor profiles
    match /chiropractor/{chiropractorId} {
      // Patients can read all chiropractor profiles
      allow read: if isAuthenticated() && isPatient();
      
      // Chiropractors can read and write their own profile
      allow read, write: if isAuthenticated() && isOwner(chiropractorId);
      
      // Admin/system can write (for initial setup)
      allow write: if isAuthenticated();
    }
    
    // Conversations collection
    match /conversations/{conversationId} {
      allow read, write: if isAuthenticated() && 
        isParticipant(resource.data.participants);
      allow create: if isAuthenticated() && 
        isParticipant(request.resource.data.participants);
      
      match /messages/{messageId} {
        allow read: if isAuthenticated() && 
          isParticipant(get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants);
        allow write: if isAuthenticated() && 
          isParticipant(get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants);
        allow create: if isAuthenticated() && 
          isOwner(request.resource.data.senderId) &&
          isParticipant(get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants);
        allow update: if isAuthenticated() && 
          isParticipant(get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants);
        allow delete: if isAuthenticated() && 
          isOwner(resource.data.senderId);
      }
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### **Step 4: Set Up Firebase Storage Rules**

Go to **Storage** → **Rules**:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    function isParticipantInConversation(conversationId) {
      return request.auth != null &&
        request.auth.uid in firestore.get(/databases/(default)/documents/conversations/$(conversationId)).data.participants;
    }
    
    // Conversation files - only participants can upload/download
    match /conversations/{conversationId}/{allPaths=**} {
      allow read, write: if isAuthenticated() && 
        isParticipantInConversation(conversationId);
    }
    
    // User profile images - only owner can upload/update
    match /users/{userId}/profile/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(userId);
    }
    
    // Chiropractor profile images - public read, owner write
    match /chiropractor/{chiropractorId}/profile/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(chiropractorId);
    }
    
    // Deny all other access
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

## 📱 App Flow / Daloy ng App

### **1. Message Screen Flow:**
```
1. User opens Messages tab
   ↓
2. App fetches from chiropractor collection
   ↓
3. Shows all chiropractors as cards
   ├── If conversation exists → Shows last message
   └── If no conversation → Shows "Tap to start conversation"
   ↓
4. User clicks chiropractor card
   ↓
5. Navigation to ChatScreen
   ├── Existing conversation → Load chat history
   └── New conversation → Load chiropractor info only
```

### **2. Chat Initiation Flow:**
```
1. User clicks chiropractor without conversation
   ↓
2. Navigate to ChatScreen with "new_{chiropractorId}"
   ↓
3. Load chiropractor details
   ↓
4. Show empty chat interface
   ↓
5. User types and sends first message
   ↓
6. Create conversation document
   ↓
7. Send message to conversation
   ↓
8. Switch to real conversation ID
   ↓
9. Start real-time message listening
```

## 🧪 Testing Checklist / Listahan ng Pagsusulit

### **Before Testing:**
- ✅ Create `chiropractor` collection with sample data
- ✅ Set up security rules
- ✅ Configure storage rules
- ✅ Ensure Firebase Authentication is working

### **Test Scenarios:**
1. **Load Chiropractors:**
   - ✅ Message screen shows all chiropractors
   - ✅ Search functionality works
   - ✅ Available/unavailable status displays correctly

2. **New Conversation:**
   - ✅ Click chiropractor without conversation
   - ✅ Navigate to ChatScreen
   - ✅ Show chiropractor info
   - ✅ Send first message creates conversation
   - ✅ Real-time messaging works

3. **Existing Conversation:**
   - ✅ Click chiropractor with conversation
   - ✅ Load chat history
   - ✅ Send new messages
   - ✅ Real-time updates work

## 🚨 Common Issues & Solutions / Mga Karaniwang Problema

### **Issue 1: "Permission denied" errors**
**Solution:** Check security rules and ensure user is authenticated

### **Issue 2: "Collection doesn't exist" errors**
**Solution:** Create the `chiropractor` collection with at least one document

### **Issue 3: "No chiropractors showing"**
**Solution:** Verify `isAvailable: true` in chiropractor documents

### **Issue 4: "Can't send messages"**
**Solution:** Check that conversation creation is working and participants are set correctly

## 📋 Sample Data Script / Script ng Sample Data

You can use this script in Firebase Console to add sample data:

```javascript
// Run this in Firebase Console → Firestore → Add document
const sampleChiropractors = [
  {
    fullName: "Dr. John Smith",
    email: "dr.smith@brightcare.com",
    specialization: "Spine Specialist",
    phoneNumber: "+1234567890",
    experience: 15,
    rating: 4.8,
    reviewCount: 120,
    isAvailable: true,
    bio: "Experienced chiropractor specializing in spinal health."
  },
  {
    fullName: "Dr. Sarah Johnson",
    email: "dr.johnson@brightcare.com",
    specialization: "Sports Medicine",
    phoneNumber: "+1234567891",
    experience: 12,
    rating: 4.9,
    reviewCount: 95,
    isAvailable: true,
    bio: "Sports medicine specialist."
  },
  {
    fullName: "Dr. Michael Brown",
    email: "dr.brown@brightcare.com",
    specialization: "Pediatric Chiropractic",
    phoneNumber: "+1234567892",
    experience: 8,
    rating: 4.7,
    reviewCount: 78,
    isAvailable: false,
    bio: "Pediatric chiropractor."
  }
];
```

---

**Ang lahat ng setup ay handa na! I-follow lang ang mga hakbang na ito sa Firebase Console at magiging functional na ang messaging system!** 🎊

This setup ensures your message screen will show all chiropractors from the `chiropractor` collection, and users can initiate conversations that get properly stored in Firestore.















