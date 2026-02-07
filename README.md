# Loan Agreement Risk Analyzer (India) - Android App

A production-ready Android application for analyzing loan agreements and detecting hidden risks in India.

## 📱 Features

### Free Features
- ✅ Upload loan agreement PDF files
- ✅ Extract basic loan information (interest rate, EMI, tenure)
- ✅ Basic risk score preview
- ✅ File storage and management

### PRO Features (₹999 one-time purchase)
- ✅ Full clause-by-clause breakdown
- ✅ Hidden fee detection
- ✅ Prepayment penalty analysis
- ✅ Effective APR calculator
- ✅ Total cost of loan calculation
- ✅ Export professional PDF reports
- ✅ Export detailed Excel cost comparison sheets
- ✅ Problematic clause identification
- ✅ Personalized recommendations

## 🏗️ Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## 📦 Tech Stack

### Core Libraries
- **AndroidX**: Core, AppCompat, ConstraintLayout
- **Material Design 3**: Modern UI components
- **Navigation Component**: Fragment navigation
- **Lifecycle Components**: ViewModel, LiveData

### Key Dependencies
- **Google Play Billing v6+**: In-app purchase system
- **PDFBox Android 2.0.27**: PDF text extraction
- **Apache POI 5.2.3**: Excel generation
- **Kotlin Coroutines**: Async operations

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Gradle 8.2+

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/zaheerabbas7892034214-ai/Loan-Agreement-Risk-Analyzer-India-.git
   cd Loan-Agreement-Risk-Analyzer-India-
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

4. **Configure Google Play Billing**
   - Create a Google Play Console account
   - Set up your app in the console
   - Create an in-app product with ID: `loanrisk_pro_unlock`
   - Set price to ₹999
   - Update `app/build.gradle.kts` with your package name if needed

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button (Shift+F10)

## 📂 Project Structure

```
app/
├── src/main/
│   ├── java/com/yourcompany/loanrisk/
│   │   ├── analytics/          # Risk analysis engine
│   │   ├── billing/            # Google Play Billing integration
│   │   ├── data/               # Data models and repositories
│   │   ├── export/             # PDF and Excel generators
│   │   ├── pdf/                # PDF extraction engine
│   │   └── presentation/       # UI (Activities, Fragments, ViewModels)
│   │       ├── splash/         # Splash screen
│   │       ├── home/           # Home screen
│   │       ├── upload/         # PDF upload
│   │       ├── summary/        # Free analysis preview
│   │       ├── paywall/        # PRO unlock screen
│   │       ├── risk/           # Risk breakdown (PRO)
│   │       ├── calculator/     # Cost calculator (PRO)
│   │       ├── export/         # Export screen (PRO)
│   │       └── settings/       # Settings
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── navigation/         # Navigation graph
│   │   ├── values/             # Strings, colors, themes
│   │   └── drawable/           # Icons and images
│   └── AndroidManifest.xml
├── build.gradle.kts            # App-level build config
└── proguard-rules.pro          # ProGuard configuration
```

## 🔧 Configuration

### Package Name
Update the package name in:
- `app/build.gradle.kts` → `applicationId`
- All Kotlin files package declarations
- `AndroidManifest.xml`

### Billing Product ID
If you change the product ID, update:
- `BillingManager.kt` → `PRODUCT_ID_PRO` constant
- Google Play Console product configuration

### Signing Configuration
For release builds, add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("your-keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 🧪 Testing

### Testing Billing
1. Add test accounts in Google Play Console
2. Use test cards for purchases
3. Test purchase flow, restoration, and PRO feature unlocking

### Testing PDF Extraction
- Use sample loan agreement PDFs
- Test with various formats and layouts
- Verify data extraction accuracy

### Test Devices
- Minimum: Android 7.0 (API 24)
- Recommended: Android 10+ for best experience

## 📱 Key Screens

1. **Splash Screen**: App logo with loading animation
2. **Home Screen**: Upload button and settings access
3. **Upload Screen**: PDF file picker and analysis trigger
4. **Summary Screen**: Basic analysis preview (FREE)
5. **Paywall Screen**: PRO feature list and purchase button
6. **Risk Breakdown**: Detailed analysis with risk score (PRO)
7. **Cost Calculator**: Financial breakdown and comparisons (PRO)
8. **Export Screen**: PDF and Excel report generation (PRO)
9. **Settings Screen**: App info and purchase restoration

## 🔒 Security Features

- ProGuard/R8 code obfuscation enabled
- Secure billing signature verification
- Encrypted purchase state storage
- File provider for secure file sharing

## 📊 Risk Analysis Engine

The app analyzes loan agreements for:
- **High interest rates** (> 15% p.a.)
- **Prepayment penalties**
- **Hidden fees** (documentation, legal, administrative)
- **Arbitration clauses**
- **Foreclosure lock-in periods**
- **Vague penalty terms**
- **Effective APR** vs stated rate

Risk scores range from 0-100:
- **0-29**: Low Risk (Green)
- **30-49**: Medium Risk (Yellow)
- **50-69**: High Risk (Orange)
- **70-100**: Critical Risk (Red)

## 📄 Export Formats

### PDF Report
- Executive summary
- Loan details
- Risk score and level
- Problematic clauses
- Recommendations
- Multi-page support

### Excel Report
- Loan summary sheet
- Cost breakdown
- Payment schedule (first 12 months)
- Hidden fees list
- Rate comparison

## 🌙 Dark Mode Support

The app fully supports Android's system dark mode using Material Design 3's DayNight themes.

## 🔗 Important Links

- **Google Play Billing Documentation**: https://developer.android.com/google/play/billing
- **PDFBox Android**: https://github.com/TomRoush/PdfBox-Android
- **Apache POI**: https://poi.apache.org/
- **Material Design 3**: https://m3.material.io/

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Support

For issues, questions, or feature requests:
- Open an issue on GitHub
- Email: support@loanrisk.com

## 🚀 Release Checklist

Before releasing to Google Play:

- [ ] Update version code and name in `build.gradle.kts`
- [ ] Configure signing for release builds
- [ ] Test on multiple devices and Android versions
- [ ] Verify billing integration with test accounts
- [ ] Test PRO feature unlocking
- [ ] Generate signed APK/AAB
- [ ] Prepare store listing assets (screenshots, descriptions)
- [ ] Submit for review

## 🎯 Future Enhancements

- Cloud sync for reports
- Comparison with RBI guidelines
- Multi-language support (Hindi, Tamil, Telugu, etc.)
- ML-based clause detection
- Legal term explanations
- Loan comparison tool

---

**Made with ❤️ for Indian consumers**
