# 🔍 Email OTP Diagnostic Guide / Gabay sa Pag-diagnose ng Email OTP

## 🚨 **Issue Identified / Nakilalang Issue**

Your emails are not being sent because the **Firebase Function is not being called** from your Android app. The logs show only deployment activities, no actual function executions.

**Ang inyong emails ay hindi napapadala dahil ang Firebase Function ay hindi tinatawag mula sa inyong Android app. Ang logs ay nagpapakita lang ng deployment activities, walang actual function executions.**

## 🔧 **Root Cause Analysis / Pagsusuri ng Ugat ng Problema**

### **Problem 1: Function Call Format**
- **Issue**: The EmailService was passing JSON string instead of HashMap
- **✅ Fixed**: Updated to pass HashMap directly to Firebase Function

### **Problem 2: Missing Logs**
- **Issue**: No detailed logging to track function calls
- **✅ Fixed**: Added comprehensive logging with emojis for easy tracking

### **Problem 3: No Function Execution**
- **Issue**: Firebase Function logs show no actual calls
- **Cause**: App might not be calling the function at all

## 🧪 **Diagnostic Steps / Mga Hakbang sa Pag-diagnose**

### **Step 1: Check Android Studio Logcat**

When you test the forgot password feature, look for these logs:

**Kapag sinusubukan ninyo ang forgot password feature, hanapin ang mga log na ito:**

```
🔥 Calling Firebase Function sendOtpEmail for: your-email@gmail.com
📧 Function data: {email=your-email@gmail.com, otp=123456, ...}
📨 Function response: {success=true, messageId=...}
✅ Cloud Function email sent successfully to: your-email@gmail.com
```

**If you see these logs**: Function is being called ✅
**If you DON'T see these logs**: Function is not being called ❌

### **Step 2: Check Firebase Function Logs**

Run this command and look for new entries:

**Patakbuhin ang command na ito at hanapin ang mga bagong entry:**

```bash
firebase functions:log --only sendOtpEmail
```

**Expected logs when function is called:**
```
Attempting to send OTP 123456 to your-email@gmail.com
Using OAuth2 authentication for Gmail
✅ OTP email sent successfully to: your-email@gmail.com
```

### **Step 3: Test Your App**

1. **Build and install the updated app**:
   ```bash
   ./gradlew assembleDebug
   # Install the APK on your device/emulator
   ```

2. **Open Android Studio Logcat**:
   - Filter by "EmailService"
   - Clear existing logs

3. **Test forgot password flow**:
   - Enter your email: `michaeljoshuataleon.edu@gmail.com`
   - Tap "Send OTP"
   - Watch the logs

4. **Check your email inbox**:
   - Look for email from "🔐 BrightCare Patient"
   - Check spam folder too

## 🔍 **Troubleshooting Scenarios / Mga Scenario sa Pag-troubleshoot**

### **Scenario A: No Function Logs in Android Studio**

**Problem**: EmailService is not calling Firebase Function
**Solutions**:

1. **Check internet connection** on device/emulator
2. **Verify Firebase project** is correctly configured
3. **Check if app has network permissions**

### **Scenario B: Function Logs Present but No Email**

**Problem**: Function is called but OAuth2 fails
**Solutions**:

1. **Check Firebase Function logs**:
   ```bash
   firebase functions:log
   ```

2. **Look for OAuth2 errors**:
   - "Failed to create access token"
   - "OAuth2 authentication failed"

3. **Verify OAuth2 credentials**:
   ```bash
   firebase functions:config:get
   ```

### **Scenario C: Function Succeeds but No Email Received**

**Problem**: Email delivery issue
**Solutions**:

1. **Check spam/junk folder**
2. **Verify email address is correct**
3. **Check Gmail delivery limits**
4. **Try different email address**

## 🛠️ **Quick Fixes / Mabilis na Pag-ayos**

### **Fix 1: Force Function Call (Test)**

Add this test code to verify function works:

**Idagdag ang test code na ito para ma-verify na gumagana ang function:**

```kotlin
// Add to EmailService for testing
suspend fun testFirebaseFunction(): Boolean {
    return sendOtpViaCloudFunction("michaeljoshuataleon.edu@gmail.com", "123456")
}
```

### **Fix 2: Manual Function Test**

Test the function directly via Firebase CLI:

**I-test ang function nang direkta sa Firebase CLI:**

```bash
# Start Firebase shell
firebase functions:shell

# In the shell, run:
sendOtpEmail({email: "michaeljoshuataleon.edu@gmail.com", otp: "123456", expiryMinutes: 10, appName: "Test"})
```

### **Fix 3: Check App Permissions**

Ensure your app has internet permission in `AndroidManifest.xml`:

**Siguraduhing may internet permission ang inyong app sa `AndroidManifest.xml`:**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 📱 **Testing Instructions / Mga Tagubilin sa Pagsubok**

### **Complete Test Procedure**

1. **Prepare for testing**:
   ```bash
   # Build the app
   ./gradlew assembleDebug
   
   # Open logcat in another terminal
   adb logcat | grep EmailService
   ```

2. **Run the test**:
   - Install and open your app
   - Go to "Forgot Password"
   - Enter: `michaeljoshuataleon.edu@gmail.com`
   - Tap "Send OTP"

3. **Monitor results**:
   - **Android Logcat**: Should show function calls
   - **Firebase Logs**: Should show function execution
   - **Email Inbox**: Should receive OTP email

4. **Expected timeline**:
   - Function call: Immediate
   - Function execution: 2-5 seconds
   - Email delivery: 10-30 seconds

## 🎯 **Success Indicators / Mga Palatandaan ng Tagumpay**

### **✅ Everything Working**
- Android logs show function calls
- Firebase logs show function execution
- Email received with OTP code
- Beautiful HTML email template

### **⚠️ Partial Success**
- Function called but OAuth2 fails → Falls back to SMTP
- Function works but email delayed → Check spam folder

### **❌ Complete Failure**
- No Android logs → App not calling function
- No Firebase logs → Function not deployed/configured
- No email received → OAuth2/SMTP both failing

## 🚀 **Next Steps / Susunod na Hakbang**

1. **Test the updated app** with the fixed EmailService
2. **Monitor both Android and Firebase logs**
3. **Check email delivery** (including spam folder)
4. **Report results** - what logs you see and what happens

## 📞 **Support Commands / Mga Command para sa Suporta**

```bash
# Check Firebase project
firebase projects:list

# Check function status
firebase functions:list

# Check function configuration
firebase functions:config:get

# View function logs
firebase functions:log

# Test function locally
firebase emulators:start --only functions

# Build and install app
./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🎊 **Expected Result / Inaasahang Resulta**

After following this guide, you should:

**Pagkatapos sundin ang gabay na ito, dapat kayong:**

1. **See detailed logs** in Android Studio showing function calls
2. **Receive actual emails** with OTP codes at `michaeljoshuataleon.edu@gmail.com`
3. **Have working OAuth2 email delivery** for production use

The updated EmailService now has proper logging and error handling to help identify exactly where the issue occurs!

**Ang na-update na EmailService ay may proper logging at error handling na para matulungan kayong malaman kung saan exactly nangyayari ang issue!**



