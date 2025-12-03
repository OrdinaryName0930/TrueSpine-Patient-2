# Onboarding Testing Guide - Quick Reference 🧪

## 🎯 How to Test Your Onboarding

### ✅ Test 1: First Time User Experience

**Steps:**
1. **Install app fresh** OR **Clear app data**:
   - Android Studio: Device Manager → Wipe Data
   - Physical device: Settings → Apps → BrightCare → Storage → Clear Data
2. **Launch app**
3. **Expected Result**: 
   - ✅ Onboarding screen appears with 3 slides
   - ✅ Can swipe between slides
   - ✅ Skip button works (jumps to last slide)
   - ✅ Next button works (advances slides)
   - ✅ Back button works (returns to previous slide)
   - ✅ "Get Started" button navigates to login

### ✅ Test 2: Returning User Experience

**Steps:**
1. **Complete onboarding once** (from Test 1)
2. **Close app completely**
3. **Relaunch app**
4. **Expected Result**:
   - ✅ Login screen appears directly
   - ✅ NO onboarding shown
   - ✅ Fast app startup

### ✅ Test 3: Reset for Testing (Developer Only)

**To test onboarding again without reinstalling:**

Add this code temporarily in `MainActivity.onCreate()`:
```kotlin
// TEMPORARY - Remove before production
OnboardingPreferences.resetOnboarding(this)
```

Then:
1. Launch app
2. Onboarding will show again
3. **Remember to remove this code!**

---

## 🔧 Debug Commands

### Check Current State
Add in `MainActivity.onCreate()`:
```kotlin
val hasSeenOnboarding = OnboardingPreferences.hasSeenOnboarding(this)
Log.d("OnboardingTest", "Has seen onboarding: $hasSeenOnboarding")
```

### View in Android Studio Logcat
1. Run app
2. Open Logcat (View → Tool Windows → Logcat)
3. Filter by "OnboardingTest"
4. See current onboarding state

---

## 📱 Quick Test Scenarios

### Scenario A: New User Journey
```
Install → Launch → See Onboarding → Complete → Login Screen
```

### Scenario B: Existing User Journey  
```
Launch → Login Screen (No Onboarding)
```

### Scenario C: App Update Journey
```
Update App → Launch → Login Screen (Onboarding preference preserved)
```

---

## ⚠️ Common Issues & Solutions

### Issue: Onboarding shows every time
**Cause**: `setOnboardingSeen()` not being called
**Check**: NavigationGraph onComplete callback
**Fix**: Verify OnboardingPreferences.setOnboardingSeen(context) is called

### Issue: Onboarding never shows
**Cause**: Preference already set to true
**Fix**: Clear app data or use reset command

### Issue: App crashes on launch
**Cause**: Context issues
**Check**: MainActivity context passing to OnboardingPreferences

---

## 🎯 Expected Behavior Summary

| User Type | First Launch | Subsequent Launches |
|-----------|-------------|-------------------|
| **New User** | Onboarding → Login | Login directly |
| **Existing User** | Login directly | Login directly |
| **After App Update** | Login directly | Login directly |
| **After Data Clear** | Onboarding → Login | Login directly |

---

## 🚀 Production Readiness Checklist

Before releasing:
- [ ] Test fresh install shows onboarding
- [ ] Test returning user skips onboarding  
- [ ] Test app update preserves preference
- [ ] Remove any debug/reset code
- [ ] Verify no crashes in either flow
- [ ] Test on multiple devices/screen sizes

---

**Your onboarding is now smart - shows once, remembers forever!**
**Ang inyong onboarding ay matalino na - nagpapakita ng isang beses, naaalala magpakailanman!**

---

**Quick Test**: Clear app data → Launch → Should see onboarding ✅


