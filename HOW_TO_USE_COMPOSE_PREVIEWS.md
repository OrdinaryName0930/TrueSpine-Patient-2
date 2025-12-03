# How to Use Compose Previews - Quick Guide 🎨

## 🎯 What are Compose Previews?

**Compose Previews** let you see your UI designs **instantly in Android Studio** without running the app on an emulator or device!

**Ang Compose Preview** ay nagpapakita ng iyong UI designs **agad sa Android Studio** nang hindi kailangan patakbuhin ang app sa emulator o device!

---

## 🚀 Quick Start / Mabilis na Simula

### Step 1: Open the File

Open either of these files in Android Studio:
- `app/src/main/java/com/brightcare/patient/ui/component/Onboarding-Component/OnboardingAdapter.kt`
- `app/src/main/java/com/brightcare/patient/ui/screens/OnboardingActivity.kt`

### Step 2: Enable Split View

Click the **"Split"** button in the top-right corner of the editor.

```
┌─────────────────────────────────────────────┐
│ File  Edit  View  [Code] [Split] [Design] │  ← Click "Split"
├─────────────────┬───────────────────────────┤
│                 │                           │
│  Your Code      │    Preview Panel         │
│  (Left Side)    │    (Right Side)          │
│                 │                           │
│  Edit here →    │    ← See changes here    │
│                 │                           │
└─────────────────┴───────────────────────────┘
```

### Step 3: See Your Previews!

The preview panel will show all available previews for that file.

---

## 📱 What You'll See / Ano ang Makikita Mo

### In OnboardingAdapter.kt (6 Previews)

```
Preview Panel:
┌──────────────────────────────────────┐
│ [Onboarding Slide 1]                 │  ← First slide with image
│ ┌────────────────────────────────┐   │
│ │    [Image]                     │   │
│ │    Your Spine, Our Care        │   │
│ │    Welcome to BrightCare...    │   │
│ └────────────────────────────────┘   │
├──────────────────────────────────────┤
│ [Onboarding Slide 2]                 │  ← Second slide
│ ┌────────────────────────────────┐   │
│ │    [Image]                     │   │
│ │    Book Your Session           │   │
│ │    Easily schedule...          │   │
│ └────────────────────────────────┘   │
├──────────────────────────────────────┤
│ [Onboarding Slide 3]                 │  ← Third slide
│ [Dot Indicators - Page 0]            │  ← Dots with page 0 active
│ [Dot Indicators - Page 1]            │  ← Dots with page 1 active
│ [Dot Indicators - Page 2]            │  ← Dots with page 2 active
└──────────────────────────────────────┘
```

### In OnboardingActivity.kt (3 Previews)

```
Preview Panel:
┌──────────────────────────────────────┐
│ [Onboarding Screen - Full]           │  ← Complete screen
│ ┌────────────────────────────────┐   │
│ │  [Skip]                        │   │
│ │     [Image]                    │   │
│ │     Title                      │   │
│ │     Content                    │   │
│ │     ● ━━━━━━ ●                 │   │
│ │     [NEXT]                     │   │
│ └────────────────────────────────┘   │
├──────────────────────────────────────┤
│ [Onboarding Screen - Night Mode]     │  ← Dark background test
│ [Onboarding Screen - Landscape]      │  ← Landscape view
└──────────────────────────────────────┘
```

---

## 🎨 View Modes / Mga Modo ng Pagtingin

### Mode 1: Split View (Recommended) ⭐

**When to use**: Active development, editing code

```
┌─────────────────────┬─────────────────────┐
│                     │                     │
│   CODE              │   PREVIEW           │
│                     │                     │
│   @Composable       │   ┌─────────────┐   │
│   fun MyUI() {      │   │   Rendered  │   │
│     Text("Hi")      │   │   Output    │   │
│   }                 │   └─────────────┘   │
│                     │                     │
│   ← Edit here       │   See here →        │
└─────────────────────┴─────────────────────┘
```

**Benefits:**
- ✅ See code and preview together
- ✅ Edit and see changes instantly
- ✅ Best for development

### Mode 2: Design View

**When to use**: Reviewing designs, showing to team

```
┌─────────────────────────────────────────┐
│         DESIGN VIEW ONLY                │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────┐  ┌─────────────┐     │
│  │  Preview 1  │  │  Preview 2  │     │
│  └─────────────┘  └─────────────┘     │
│                                         │
│  ┌─────────────┐  ┌─────────────┐     │
│  │  Preview 3  │  │  Preview 4  │     │
│  └─────────────┘  └─────────────┘     │
│                                         │
└─────────────────────────────────────────┘
```

**Benefits:**
- ✅ See all previews at once
- ✅ Grid layout for comparison
- ✅ Best for reviewing

### Mode 3: Code Only + Preview Panel

**When to use**: Large screen, want maximum space

```
┌─────────────────────────────────────────┐
│              CODE VIEW                  │
│                                         │
│  @Composable                            │
│  fun MyUI() {                           │
│    Column {                             │
│      Text("Hello")                      │
│    }                                    │
│  }                                      │
│                                         │
└─────────────────────────────────────────┘
             ↓ Opens in separate panel ↓
┌─────────────────────────────────────────┐
│          PREVIEW PANEL (Floating)       │
│  ┌─────────────────────────────────┐   │
│  │      Rendered Preview           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 🎬 Interactive Features / Mga Interactive na Feature

### 1. Refresh Preview

Click the **🔄 Build & Refresh** button if preview is stale:

```
Preview Panel Top Bar:
┌─────────────────────────────────────┐
│ [🔄 Build & Refresh] [⚙️ Settings]  │  ← Click here
└─────────────────────────────────────┘
```

### 2. Select Specific Preview

If multiple previews exist, use the dropdown:

```
┌─────────────────────────────────────┐
│ [▼ Onboarding Slide 1            ]  │  ← Click to switch
│     Onboarding Slide 2              │
│     Onboarding Slide 3              │
│     Dot Indicators - Page 0         │
└─────────────────────────────────────┘
```

### 3. Zoom In/Out

Use the zoom controls:

```
Preview Controls:
┌──────────────────────────┐
│ [➖] 100% [➕]           │  ← Zoom controls
└──────────────────────────┘
```

### 4. Interactive Mode (Optional)

Click **"Start Interactive Preview"** to:
- Click buttons (within preview)
- See hover effects
- Test interactions

**Note**: Not all interactions work in preview mode, but useful for testing basic UI behavior.

---

## 🔥 Real-Time Updates / Real-Time na Pag-update

### How It Works

```
You Type:                      Preview Shows:
─────────────────────────────────────────────

Text("Hello")          →       Hello

Text(                  →       Hello World
  "Hello World",
  fontSize = 20.sp
)

Text(                  →       Hello World
  "Hello World",                (now bigger!)
  fontSize = 32.sp
)
```

**Changes appear instantly** as you type! ✨
**Ang mga pagbabago ay lalabas agad** habang nagta-type ka! ✨

---

## 📊 Preview Options / Mga Opsyon sa Preview

### Standard Preview
```kotlin
@Preview
@Composable
fun SimplePreview() {
    Text("Hello")
}
```
Result: Basic preview with default settings

### Preview with Background
```kotlin
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF  // White
)
@Composable
fun PreviewWithBackground() {
    Text("Hello")
}
```
Result: Preview with white background

### Preview with System UI
```kotlin
@Preview(showSystemUi = true)
@Composable
fun PreviewWithStatusBar() {
    Text("Hello")
}
```
Result: Shows status bar and navigation bar

### Preview with Custom Size
```kotlin
@Preview(
    widthDp = 400,
    heightDp = 800
)
@Composable
fun PreviewCustomSize() {
    Text("Hello")
}
```
Result: Preview at 400×800 dp

### Multiple Previews
```kotlin
@Preview(name = "Light Theme")
@Composable
fun PreviewLight() {
    MyUI()
}

@Preview(name = "Dark Theme")
@Composable
fun PreviewDark() {
    MyUI()
}
```
Result: Two previews shown side-by-side

---

## 🎯 Onboarding-Specific Previews / Mga Preview para sa Onboarding

### View All Slides

```
File: OnboardingAdapter.kt
Mode: Design View

You'll see:
├─ Slide 1: "Your Spine, Our Care"     (with s1.jpg image)
├─ Slide 2: "Book Your Session"        (with s2.jpg image)
├─ Slide 3: "Feel Better, Move Better" (with s3.jpg image)
├─ Dots at Page 0: ━━━━━━ ● ●
├─ Dots at Page 1: ● ━━━━━━ ●
└─ Dots at Page 2: ● ● ━━━━━━
```

### View Full Screen

```
File: OnboardingActivity.kt
Mode: Split View

You'll see:
- Complete onboarding screen
- Skip button in top-right
- Slide content in center
- Dot indicators at bottom
- Next button at bottom
```

---

## 🛠️ Troubleshooting / Pag-troubleshoot

### Problem 1: Preview Not Showing

```
Preview Panel:
┌──────────────────────────┐
│                          │
│  (blank or empty)        │
│                          │
└──────────────────────────┘
```

**Solutions:**
1. Click **🔄 Build & Refresh**
2. Build → Rebuild Project
3. File → Invalidate Caches → Restart
4. Check for compilation errors in code

### Problem 2: Preview Shows "Rendering Problems"

```
Preview Panel:
┌──────────────────────────┐
│  ⚠️ Rendering Problems   │
│  Click for details       │
└──────────────────────────┘
```

**Solutions:**
1. Click the error to see details
2. Fix any missing imports
3. Ensure @Preview function is marked as @Composable
4. Check if Context is needed (use LocalContext.current)

### Problem 3: Images Not Showing in Preview

```
Preview Panel:
┌──────────────────────────┐
│  Title Text              │
│  Content Text            │
│  (no image shown)        │
└──────────────────────────┘
```

**This is Normal!** 
- Assets may not load in preview mode
- The layout and text will show correctly
- Images will load fine when running on device
- Preview is for layout testing, not asset testing

### Problem 4: Preview is Slow

**Symptoms:**
- Long render times
- IDE freezing
- High CPU usage

**Solutions:**
1. Comment out unused @Preview functions
2. Reduce number of active previews
3. Use smaller heightDp/widthDp values
4. Close other Android Studio windows
5. Increase IDE memory: Help → Edit Custom VM Options

---

## 💡 Pro Tips / Mga Propesyonal na Tip

### Tip 1: Use Preview for Layout Only
✅ Test spacing, colors, text sizes  
✅ Verify component positioning  
❌ Don't rely on it for animations  
❌ Don't test navigation in preview  

### Tip 2: Name Your Previews Well
```kotlin
// ✅ Good - Descriptive
@Preview(name = "Onboarding Slide 1 - Portrait")

// ❌ Bad - Generic
@Preview(name = "Preview1")
```

### Tip 3: Group Related Previews
```kotlin
// Slides
@Preview fun PreviewSlide1() { ... }
@Preview fun PreviewSlide2() { ... }
@Preview fun PreviewSlide3() { ... }

// Dots
@Preview fun PreviewDots0() { ... }
@Preview fun PreviewDots1() { ... }
@Preview fun PreviewDots2() { ... }
```

### Tip 4: Test Different Sizes
```kotlin
@Preview(widthDp = 360, heightDp = 640)  // Small phone
@Preview(widthDp = 400, heightDp = 800)  // Medium phone
@Preview(widthDp = 600, heightDp = 1000) // Large phone
```

### Tip 5: Use Interactive Preview Sparingly
- Interactive mode uses more resources
- Only enable when testing specific interactions
- Turn off when just viewing layouts

---

## 🎓 Learning Workflow / Daloy ng Pag-aaral

### For Beginners / Para sa mga Nagsisimula
1. Open file with @Preview
2. Click "Split" view
3. Change some text
4. Watch preview update
5. Experiment with colors
6. Try different sizes

### For Developers / Para sa mga Developer
1. Use previews for rapid iteration
2. Test edge cases with different previews
3. Verify responsive behavior
4. Show designs to team without running app
5. Catch UI bugs early

---

## 📸 Screenshot Guide / Gabay sa Screenshot

### Taking Screenshots from Preview

1. Right-click on preview
2. Select "Copy Image"
3. Paste into documentation, Slack, etc.

**Use Cases:**
- Share designs with team
- Add to documentation
- Create UI specs
- Design reviews

---

## ✅ Quick Checklist / Mabilis na Checklist

Before using previews:
- [ ] File contains @Composable functions
- [ ] @Preview annotation is added
- [ ] Function has no parameters (or default values)
- [ ] Theme wrapper is included (BrightCarePatientTheme)
- [ ] No compilation errors

When viewing previews:
- [ ] Split or Design view is active
- [ ] Preview panel is visible
- [ ] Build is up to date
- [ ] Zoom level is comfortable
- [ ] Correct preview is selected

---

## 🎉 Summary / Buod

### What You Learned / Ano ang Natutunan Mo
✅ How to open preview panel  
✅ How to switch between view modes  
✅ How to see multiple previews  
✅ How to troubleshoot issues  
✅ How to use previews effectively  

### What You Can Do Now / Ano ang Magagawa Mo Ngayon
✅ See UI changes instantly  
✅ Test different screen sizes  
✅ Verify designs without emulator  
✅ Share previews with team  
✅ Develop UI faster  

---

**Your onboarding component has 9 amazing previews ready to use!**
**Ang iyong onboarding component ay may 9 magagandang preview na handa nang gamitin!**

Open the files and start exploring! 🚀

**Date**: November 2024  
**Version**: 1.0  
**Status**: Ready to Use



