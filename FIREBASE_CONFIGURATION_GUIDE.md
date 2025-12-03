# Firebase Configuration Guide for Social Login

## 🔥 Firebase Console Setup Required

You need to configure several settings in your Firebase Console to make both Google and Facebook sign-in work properly.

### 1. 🔐 Firebase Authentication Settings

#### Step 1: Enable Authentication Providers
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **brightcare-5a7a1**
3. Navigate to **Authentication** → **Sign-in method**

#### Step 2: Configure Google Sign-In
1. Click on **Google** provider
2. Click **Enable**
3. **Project support email**: Enter your email address
4. **Web SDK configuration**: Should already be configured
5. Click **Save**

#### Step 3: Configure Facebook Sign-In
1. Click on **Facebook** provider  
2. Click **Enable**
3. You'll need to provide:
   - **App ID**: Get from Facebook Developers Console
   - **App Secret**: Get from Facebook Developers Console
4. **OAuth redirect URI**: Copy this URL (you'll need it for Facebook App setup):
   ```
   https://brightcare-5a7a1.firebaseapp.com/__/auth/handler
   ```
5. Click **Save**

### 2. 📱 Facebook App Setup

#### Step 1: Create Facebook App
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Click **Create App**
3. Choose **Consumer** as app type
4. Fill in details:
   - **App Name**: BrightCare Patient
   - **Contact Email**: Your email

#### Step 2: Add Facebook Login Product
1. In your Facebook App dashboard
2. Go to **Products** → **Add Product**
3. Find **Facebook Login** and click **Set Up**

#### Step 3: Configure Facebook Login Settings
1. Go to **Facebook Login** → **Settings**
2. Add **Valid OAuth Redirect URIs**:
   ```
   https://brightcare-5a7a1.firebaseapp.com/__/auth/handler
   ```
3. **Client OAuth Settings**:
   - ✅ Use Strict Mode for Redirect URIs
   - ✅ Enforce HTTPS

#### Step 4: Get App Credentials
1. Go to **Settings** → **Basic**
2. Copy your **App ID**
3. Copy your **App Secret** (click Show)

#### Step 5: Update Your App Configuration
Replace the placeholders in `app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">BrightCare Patient</string>
    
    <!-- Facebook Configuration -->
    <string name="facebook_app_id">YOUR_FACEBOOK_APP_ID_HERE</string>
    <string name="facebook_client_token">YOUR_FACEBOOK_APP_SECRET_HERE</string>
    <string name="fb_login_protocol_scheme">fbYOUR_FACEBOOK_APP_ID_HERE</string>
</resources>
```

**Example with real values:**
```xml
<string name="facebook_app_id">1234567890123456</string>
<string name="facebook_client_token">abcdef1234567890abcdef1234567890</string>
<string name="fb_login_protocol_scheme">fb1234567890123456</string>
```

#### Step 6: Add App ID and Secret to Firebase
1. Back in Firebase Console → **Authentication** → **Sign-in method**
2. Click **Facebook** provider
3. Enter your **App ID** and **App Secret** from Facebook
4. Click **Save**

### 3. 🔒 Firebase Security Settings

#### Important: Prevent Multiple Accounts per Email
1. In Firebase Console → **Authentication** → **Settings**
2. Go to **User account linking**
3. Select **"Prevent creation of multiple accounts with the same email address"**
4. This will help enforce the email uniqueness we implemented in code

### 4. 🧪 Testing Configuration

#### Test Google Sign-In:
1. Build and run your app
2. Try signing up with email/password first
3. Then try Google sign-in with the same email
4. Should show "Email already in use" error

#### Test Facebook Sign-In:
1. Make sure Facebook App is in **Development Mode** for testing
2. Add test users in Facebook App → **Roles** → **Test Users**
3. Or switch to **Live Mode** (requires app review)

### 5. 🚨 Common Issues and Solutions

#### Google Sign-In Issues:

**Issue**: "Invalid Google credential type"
**Solution**: 
- Verify Web Client ID is correct in your code
- Check if Google Sign-In is enabled in Firebase Console

**Issue**: "No Google account found"
**Solution**:
- Make sure device has Google Play Services
- Add a Google account to the test device

#### Facebook Sign-In Issues:

**Issue**: "Facebook login failed"
**Solution**:
- Verify App ID and App Secret are correct
- Check OAuth redirect URI in Facebook App settings
- Ensure Facebook Login product is added to your app

**Issue**: "App not in development mode"
**Solution**:
- Add test users in Facebook App dashboard
- Or submit for app review to go live

#### Email Duplication Issues:

**Issue**: Same email works with different providers
**Solution**:
- ✅ Already implemented in code
- ✅ Enable "Prevent multiple accounts" in Firebase Console
- The app will now show "Email already in use" error

### 6. 📋 Verification Checklist

#### Firebase Console:
- [ ] Google Sign-In provider enabled
- [ ] Facebook Sign-In provider enabled with correct App ID/Secret
- [ ] "Prevent multiple accounts per email" enabled
- [ ] OAuth redirect URI copied for Facebook setup

#### Facebook App:
- [ ] Facebook Login product added
- [ ] OAuth redirect URI configured
- [ ] App ID and App Secret obtained
- [ ] Test users added (for development mode)

#### App Configuration:
- [ ] Facebook App ID added to strings.xml
- [ ] Facebook App Secret added to strings.xml
- [ ] Facebook protocol scheme configured
- [ ] AndroidManifest.xml has Facebook activities

### 7. 🔍 Debug Information

#### Check Logcat for these messages:
- **Google Sign-In**: Look for "Google Sign-In" tags
- **Facebook Sign-In**: Look for "Facebook Sign-In" tags
- **Email Check**: Look for "Checking if email exists" messages
- **Firebase Auth**: Look for Firebase authentication success/failure

#### Useful Log Commands:
```bash
# Filter for authentication logs
adb logcat | grep -E "(PatientSignUpRepository|Firebase|Google|Facebook)"

# Filter for specific errors
adb logcat | grep -E "(ERROR|WARN)" | grep -E "(Auth|Sign)"
```

### 8. 🚀 Production Deployment

#### Before going live:
1. **Facebook App**: Submit for app review or add all users as testers
2. **Firebase**: Review security rules and user management
3. **Testing**: Test on multiple devices and Android versions
4. **Analytics**: Set up Firebase Analytics for login events

## 📞 Next Steps

1. **Complete Facebook App setup** with the steps above
2. **Test both social logins** thoroughly
3. **Verify email duplication handling** works correctly
4. **Monitor Firebase Console** for authentication events

The duplicate email handling is now implemented! When a user tries to sign up with Google/Facebook using an email that's already registered with email/password, they'll see the "Email already in use" error message below the email field.

Ang duplicate email handling ay na-implement na ngayon! Kapag sinubukan ng user na mag-sign up gamit ang Google/Facebook na may email na naka-register na sa email/password, makikita nila ang "Email already in use" error message sa ilalim ng email field.









