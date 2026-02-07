# Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Prerequisites
```bash
# Required
- Android Studio Hedgehog (2023.1.1+)
- JDK 17
- Android SDK 34
- Git

# Recommended
- 8GB RAM minimum
- 10GB free disk space
```

### Step 1: Clone & Open
```bash
git clone https://github.com/zaheerabbas7892034214-ai/Loan-Agreement-Risk-Analyzer-India-.git
cd Loan-Agreement-Risk-Analyzer-India-
```

Open in Android Studio → Wait for Gradle sync

### Step 2: Quick Configuration

#### Option A: Run Immediately (Test Mode)
```bash
# No changes needed - runs with default settings
./gradlew assembleDebug
```

#### Option B: Configure for Production
1. **Update Package Name** (Optional)
   - File: `app/build.gradle.kts`
   - Change: `applicationId = "com.yourcompany.loanrisk"`

2. **Setup Billing** (For PRO features)
   - Create app in Google Play Console
   - Add in-app product: `loanrisk_pro_unlock` (₹999)
   - Test with Google Play test accounts

### Step 3: Build & Run
```bash
# Debug build
./gradlew assembleDebug

# Or use Android Studio:
# Click Run (▶️) or press Shift+F10
```

## 📱 Test the App

### Test Flow:
1. **Launch** → Splash screen (2.5s)
2. **Home** → Tap "Upload Agreement"
3. **Upload** → Select any PDF file
4. **Summary** → See basic analysis (FREE)
5. **Paywall** → See PRO features
6. **Settings** → View app info

### Sample Test Files
- Create a text file with loan terms
- Convert to PDF
- Test with various formats

## 🔧 Common Issues & Fixes

### Issue 1: Build Failed
```bash
# Solution: Clean and rebuild
./gradlew clean
./gradlew build
```

### Issue 2: Dependencies Not Downloading
```bash
# Solution: Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew --refresh-dependencies
```

### Issue 3: PDFBox Errors
```bash
# Solution: Ensure correct version
# Check app/build.gradle.kts has:
implementation("com.tom-roush:pdfbox-android:2.0.27.0")
```

### Issue 4: Navigation Errors
```bash
# Solution: Rebuild project
Build → Clean Project
Build → Rebuild Project
```

## 📂 Project Structure Overview
```
app/
├── src/main/
│   ├── java/com/yourcompany/loanrisk/
│   │   ├── analytics/          # Risk analysis logic
│   │   ├── billing/            # Google Play Billing
│   │   ├── data/               # Models
│   │   ├── export/             # PDF/Excel export
│   │   ├── pdf/                # PDF extraction
│   │   └── presentation/       # UI (Fragments, ViewModels)
│   ├── res/
│   │   ├── layout/             # 11 XML layouts
│   │   ├── navigation/         # Navigation graph
│   │   └── values/             # Strings, colors, themes
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## 🎯 Key Files to Know

### Entry Points
- `MainActivity.kt` - App entry
- `SplashFragment.kt` - First screen
- `nav_graph.xml` - Screen flow

### Core Logic
- `PdfExtractor.kt` - PDF parsing
- `RiskAnalyzer.kt` - Risk calculation
- `BillingManager.kt` - Monetization

### UI Screens
- 10 Fragments in `presentation/` package
- 11 XML layouts in `res/layout/`

## 🧪 Testing Checklist

### Basic Flow ✓
- [ ] App launches successfully
- [ ] Splash screen appears
- [ ] Home screen loads
- [ ] Upload screen opens
- [ ] PDF picker works

### PRO Features (After Billing Setup) ✓
- [ ] Paywall displays
- [ ] Purchase flow works
- [ ] PRO features unlock
- [ ] Restore works

### Export Features ✓
- [ ] PDF report generates
- [ ] Excel sheet creates
- [ ] Files can be shared

## 🔑 Important Configuration

### Change Package Name
1. Update in `app/build.gradle.kts`
2. Refactor package in Android Studio
3. Update `AndroidManifest.xml`

### Add Signing for Release
```kotlin
// In app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "your-password"
            keyAlias = "your-alias"
            keyPassword = "your-password"
        }
    }
}
```

### Configure Billing
```kotlin
// In BillingManager.kt
const val PRODUCT_ID_PRO = "loanrisk_pro_unlock"  // Change if needed
```

## 📖 Next Steps

### For Development
1. Read `README.md` for detailed docs
2. Check `IMPLEMENTATION_SUMMARY.md` for architecture
3. Review code comments in key files
4. Test on multiple devices

### For Production
1. Configure signing certificate
2. Setup Google Play Console
3. Add billing product
4. Test with real accounts
5. Prepare store listing
6. Submit for review

## 💡 Pro Tips

### Development
- Use Android Studio's Layout Inspector
- Test on different screen sizes
- Enable strict mode for debugging
- Use Logcat for troubleshooting

### Performance
- PDF processing is async (Coroutines)
- Large PDFs may take time
- Test with 1-10 page PDFs first

### Billing
- Test purchases are free
- Use test accounts from Play Console
- Real purchases need production app

## 🆘 Need Help?

### Resources
- Android Developers: https://developer.android.com
- Billing Guide: https://developer.android.com/google/play/billing
- Material Design: https://m3.material.io

### Common Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Check dependencies
./gradlew dependencies
```

## ✅ Verification

Run this to verify setup:
```bash
# Check structure
ls -la app/src/main/java/com/yourcompany/loanrisk/
ls -la app/src/main/res/layout/

# Count files
find app/src/main/java -name "*.kt" | wc -l  # Should be ~20
find app/src/main/res/layout -name "*.xml" | wc -l  # Should be 11
```

---

**You're ready to start!** 🎉

Run the app and explore the features.
