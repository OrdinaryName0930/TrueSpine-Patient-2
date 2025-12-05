# 🚨 URGENT: Deploy Firestore Rules to Fix Permission Errors

## Current Issue / Kasalukuyang Problema

The app is getting **PERMISSION_DENIED** errors because the Firestore security rules haven't been deployed to Firebase yet. The user can log in successfully, but cannot access their profile data.

**Ang app ay nakakakuha ng PERMISSION_DENIED errors dahil hindi pa na-deploy ang Firestore security rules sa Firebase. Makakapag-log in ang user pero hindi ma-access ang kanilang profile data.**

## Error Details from Logs:
```
Listen for Query(target=Query(client/JDL4hX503zOwzhSttsKitUnrsWm2 order by __name__);limitType=LIMIT_TO_FIRST) failed: 
Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

## 🔥 IMMEDIATE SOLUTION - Deploy Rules Manually

### Step 1: Go to Firebase Console
1. Open your browser and go to: https://console.firebase.google.com/
2. Select your project: **truespine-e8576**
3. Click on **Firestore Database** in the left sidebar
4. Click on the **Rules** tab

### Step 2: Replace the Current Rules
Copy and paste the following rules (replace everything in the rules editor):

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to chiropractors collection for all authenticated users
    // Magbigay ng read access sa chiropractors collection para sa lahat ng authenticated users
    match /chiropractors/{chiropractorId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    
    // Allow users to read and write their own user data
    // Payagan ang mga users na magbasa at magsulat ng kanilang sariling user data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write their own profile data
    // Payagan ang mga authenticated users na magbasa at magsulat ng kanilang profile data
    match /userProfiles/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write their own client profile data
    // Payagan ang mga authenticated users na magbasa at magsulat ng kanilang client profile data
    match /client/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write conversations they participate in
    // Payagan ang mga authenticated users na magbasa at magsulat ng conversations na kasali sila
    match /conversations/{conversationId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid in resource.data.participants || 
         request.auth.uid in request.resource.data.participants);
    }
    
    // Allow authenticated users to read and write messages in conversations they participate in
    // Payagan ang mga authenticated users na magbasa at magsulat ng messages sa conversations na kasali sila
    match /conversations/{conversationId}/messages/{messageId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
    }
    
    // Allow authenticated users to read and write their own appointments
    // Payagan ang mga authenticated users na magbasa at magsulat ng kanilang appointments
    match /appointments/{appointmentId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid == resource.data.patientId || 
         request.auth.uid == resource.data.chiropractorId);
    }
    
    // Allow authenticated users to read and write their own bookings
    // Payagan ang mga authenticated users na magbasa at magsulat ng kanilang bookings
    match /bookings/{bookingId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid == resource.data.patientId || 
         request.auth.uid == resource.data.chiropractorId);
    }
    
    // Default rule: deny all other access
    // Default rule: tanggihan ang lahat ng ibang access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Step 3: Publish the Rules
1. Click the **"Publish"** button
2. Confirm the deployment

## 🧪 Test After Deployment

After publishing the rules:

1. **Close and restart your app** completely
2. **Log in again** with the same credentials
3. **Check the logs** - you should no longer see PERMISSION_DENIED errors
4. **Try accessing profile data** - it should work now

## 📱 Alternative: Quick CLI Deployment (if available)

If you have Firebase CLI working, you can also run:
```bash
firebase login
firebase deploy --only firestore:rules
```

## ✅ Expected Results After Deployment

After successful deployment, you should see:
- ✅ No more PERMISSION_DENIED errors in logs
- ✅ Profile data loads successfully
- ✅ Chiropractor data loads successfully
- ✅ User can access their own data

## 🚨 If Still Getting Errors

If you still get permission errors after deployment:

1. **Wait 1-2 minutes** for rules to propagate
2. **Force close and restart the app**
3. **Clear app data/cache** if necessary
4. **Check Firebase Console** to ensure rules were saved correctly

## 📞 Need Help?

If you're having trouble with the Firebase Console:
1. Make sure you're logged into the correct Google account
2. Verify you have admin access to the `truespine-e8576` project
3. Try using an incognito/private browser window

## Summary / Buod

**The problem:** Firestore rules not deployed = Permission denied errors
**The solution:** Deploy the rules via Firebase Console
**The result:** App will work properly with proper permissions

**Ang problema:** Hindi na-deploy ang Firestore rules = Permission denied errors
**Ang solusyon:** I-deploy ang rules via Firebase Console  
**Ang resulta:** Gagana na ng maayos ang app with proper permissions

---

**🔥 THIS IS URGENT - The app won't work properly until these rules are deployed!**
**🔥 ITO AY URGENT - Hindi gagana ng maayos ang app hanggang hindi na-deploy ang mga rules na ito!**