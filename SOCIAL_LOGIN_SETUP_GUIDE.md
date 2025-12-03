# Social Login Setup Guide - Google & Facebook

## Overview / Pangkalahatang Paglalarawan

This guide will help you complete the setup for Google and Facebook sign-in functionality in your BrightCare Patient app.

Ang gabay na ito ay tutulong sa inyo na makumpleto ang setup para sa Google at Facebook sign-in functionality sa inyong BrightCare Patient app.

## ✅ What's Already Implemented / Ano na ang Na-implement

### Google Sign-In ✅
- ✅ Google Web Client ID configured from your `google-services.json`
- ✅ Credential Manager API implementation
- ✅ Firebase Authentication integration
- ✅ Proper error handling with detailed logging
- ✅ Activity context handling

### Facebook Sign-In ✅
- ✅ Facebook SDK dependency added
- ✅ Facebook Login implementation with callbacks
- ✅ Firebase Authentication integration
- ✅ AndroidManifest.xml configuration
- ✅ Application class initialization

## 🔧 Setup Steps Required / Mga Hakbang na Kailangan

### 1. Facebook App Configuration

You need to create a Facebook App and get the App ID and Client Token:

#### Step 1: Create Facebook App
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Click "Create App" 
3. Choose "Consumer" app type
4. Fill in your app details:
   - **App Name**: BrightCare Patient
   - **Contact Email**: Your email

#### Step 2: Configure Facebook Login
1. In your Facebook App dashboard, go to "Products" → "Facebook Login"
2. Click "Settings" under Facebook Login
3. Add these **Valid OAuth Redirect URIs**:
   ```
   https://brightcare-5a7a1.firebaseapp.com/__/auth/handler
   ```

#### Step 3: Get App ID and Client Token
1. Go to "Settings" → "Basic"
2. Copy your **App ID**
3. Copy your **App Secret** (this will be your Client Token)

#### Step 4: Update strings.xml
Replace the placeholders in `app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">BrightCare Patient</string>
    
    <!-- Facebook Configuration -->
    <string name="facebook_app_id">YOUR_ACTUAL_FACEBOOK_APP_ID</string>
    <string name="facebook_client_token">YOUR_ACTUAL_FACEBOOK_CLIENT_TOKEN</string>
    <string name="fb_login_protocol_scheme">fbYOUR_ACTUAL_FACEBOOK_APP_ID</string>
</resources>
```

**Example:**
```xml
<string name="facebook_app_id">1234567890123456</string>
<string name="facebook_client_token">abcdef1234567890abcdef1234567890</string>
<string name="fb_login_protocol_scheme">fb1234567890123456</string>
```

### 2. Firebase Console Configuration

#### For Google Sign-In:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `brightcare-5a7a1`
3. Go to "Authentication" → "Sign-in method"
4. Enable "Google" provider
5. Your Web SDK configuration should already be set up

#### For Facebook Sign-In:
1. In Firebase Console → "Authentication" → "Sign-in method"
2. Enable "Facebook" provider
3. Enter your Facebook **App ID** and **App Secret**
4. Copy the OAuth redirect URI and add it to your Facebook App settings

### 3. Testing the Implementation

#### Test Google Sign-In:
1. Build and run the app
2. Go to Sign Up screen
3. Tap the Google sign-in button
4. Select a Google account
5. Should redirect to CompleteProfile screen on success

#### Test Facebook Sign-In:
1. Make sure Facebook app is installed on test device (or use web login)
2. Tap the Facebook sign-in button
3. Login with Facebook credentials
4. Should redirect to CompleteProfile screen on success

## 🐛 Troubleshooting / Pag-troubleshoot

### Google Sign-In Issues:

**Problem**: "No Google account found"
**Solution**: 
- Make sure Google Play Services is updated
- Add a Google account to the device
- Check if the SHA-1 fingerprint is correctly configured in Firebase

**Problem**: "Invalid Google credential type"
**Solution**:
- Verify the Web Client ID is correct
- Check Firebase project configuration
- Ensure Google Sign-In is enabled in Firebase Console

### Facebook Sign-In Issues:

**Problem**: "Facebook login failed"
**Solution**:
- Verify Facebook App ID and Client Token are correct
- Check if Facebook Login is enabled in your Facebook App
- Ensure redirect URI is correctly configured

**Problem**: "App not in development mode"
**Solution**:
- Add test users in Facebook App dashboard
- Or switch Facebook App to "Live" mode (requires app review)

### General Issues:

**Problem**: App crashes on social login
**Solution**:
- Check Logcat for detailed error messages
- Verify all dependencies are properly added
- Ensure AndroidManifest.xml is correctly configured

## 📱 Testing on Different Environments

### Debug Testing:
- Use debug SHA-1 fingerprint in Firebase
- Facebook App can be in "Development" mode
- Add test users in Facebook App dashboard

### Release Testing:
- Use release SHA-1 fingerprint in Firebase
- Facebook App needs to be "Live" or add release key hash
- Test with real user accounts

## 🔐 Security Notes / Mga Tala sa Security

1. **Never commit sensitive keys to version control**
2. **Use different Facebook Apps for debug/release builds**
3. **Regularly rotate Client Tokens**
4. **Monitor authentication logs in Firebase Console**

## 📋 Verification Checklist / Checklist para sa Verification

### Google Sign-In:
- [ ] Web Client ID is correctly set in repository
- [ ] Google Sign-In enabled in Firebase Console
- [ ] SHA-1 fingerprint added to Firebase project
- [ ] Google Play Services available on test device

### Facebook Sign-In:
- [ ] Facebook App created and configured
- [ ] App ID and Client Token added to strings.xml
- [ ] Facebook Login enabled in Facebook App
- [ ] OAuth redirect URI configured in Facebook App
- [ ] Facebook provider enabled in Firebase Console

### General:
- [ ] Internet permission in AndroidManifest.xml
- [ ] Facebook SDK initialized in Application class
- [ ] Social login buttons call correct ViewModel methods
- [ ] Navigation works correctly after successful login

## 🚀 Next Steps / Mga Susunod na Hakbang

1. **Complete Facebook App setup** with your actual App ID and Client Token
2. **Test both social logins** on a physical device
3. **Add error handling** for specific edge cases if needed
4. **Implement logout functionality** if required
5. **Add analytics tracking** for social login events

## 📞 Support / Suporta

If you encounter any issues:

1. **Check Logcat** for detailed error messages
2. **Verify all configuration steps** are completed
3. **Test on different devices** to isolate device-specific issues
4. **Check Firebase Console logs** for authentication events

The implementation is now complete and ready for testing! Just complete the Facebook App configuration and you'll have fully working social logins.

Ang implementation ay kumpleto na ngayon at handa na para sa testing! Kumpletuhin lang ang Facebook App configuration at magkakaroon na kayo ng fully working social logins.









