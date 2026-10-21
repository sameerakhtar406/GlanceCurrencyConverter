# GlanceCurrencyConverter (RoamRate) ⚡ 

> **Ultra-fast, offline-first Android currency converter powered by Jetpack Glance & Compose.**  
> Designed for globetrotters, frequent flyers, and digital nomads who need instant price conversions on the go without relying on an active internet connection or opening heavy apps.

---

![me](https://github.com/sameerakhtar406/GlanceCurrencyConverter/blob/main/1792419249112.1792568932399.publer.com.gif)

## ✨ Key Features

* **⚡ 1-Tap Overlay Dialog:** Tap the home screen widget to trigger a translucent conversion card with an auto-focused numeric keyboard. Zero launch latency.
* **📲 Jetpack Glance Home Widget:** Minimalist, dynamic Material 3 widget displaying your active currency pair and live exchange rates directly on your home screen.
* **✈️ 100% Offline-First:** Calculates cross-rate conversions using cached rates. No internet connection required while traveling abroad.
* **🔄 Automatic Background Sync:** Integrates with Android `WorkManager` to silently update exchange rates once daily whenever Wi-Fi or mobile data is connected.
* **🔔 Quick Settings Tile:** Access the instant converter modal directly from the Android notification shade from inside any app.
* **🎨 Modern Compose UI:** Clean Material 3 design supporting dynamic system color schemes and smooth layout transitions.

---

## 📱 Screenshots & Demo

| Home Screen Widget | 1-Tap Quick Convert Dialog | Main Configuration App |
|:------------------:|:--------------------------:|:----------------------:|
|<img width="1187" height="2513" alt="Screenshot_20261019_170803" src="https://github.com/user-attachments/assets/d56c36d0-8757-4a8c-94c3-78cf92a3e44d" />|<img width="1187" height="2513" alt="Screenshot_20261019_170940" src="https://github.com/user-attachments/assets/826d43c2-3907-4102-b456-d40afc529de8" />|<img width="1187" height="2513" alt="Screenshot_20261019_170220" src="https://github.com/user-attachments/assets/171b0225-5d64-45be-a79a-c708056e5c9e" />



---

## 🏗️ Architecture & Tech Stack
```
┌─────────────────────────────────────────────────────────────┐
│                      Exchange Rate API                      │
│                (Frankfurter / ExchangeRate-API)             │
└──────────────────────────────┬──────────────────────────────┘
                               │ Syncs via WorkManager
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Jetpack DataStore Preferences               │
│         - Base USD Rate Matrix                              │
│         - Selected Currency Pairs (e.g. AED ➔ INR)          │
│         - Last Sync Timestamp                               │
└───────────────┬─────────────────────────────┬───────────────┘
                │                             │
                ▼                             ▼
  ┌─────────────────────────┐   ┌─────────────────────────┐
  │    Main Configuration   │   │  Jetpack Glance Widget  │
  │  (Compose + Keypad UI)  │   │   (Quick Modal Trigger) │
  └─────────────────────────┘   └─────────────────────────┘
```

* **Language:** 100% Kotlin
* **UI Framework:** Jetpack Compose + Material 3
* **AppWidget:** Jetpack Glance (`androidx.glance`)
* **Local Storage:** Jetpack DataStore Preferences
* **Network & Sync:** Ktor Client + Android WorkManager
* **Minimum SDK:** Android 13 (API 33) | **Target SDK:** Android 15 (API 35)

---

## 🚀 Quick Setup & Installation

### Option 1: Install APK (For Users)

1. Head over to the [Releases Page](https://github.com/sameerakhtar406/GlanceCurrencyConverter/releases/tag/v1.0.0) and download `CurrencyConverter-v1.0.apk`.
2. Tap the downloaded file on your Android phone to install.
3. If prompted by Android, enable **Allow from this source**.
4. Add the **Currency Converter** widget to your phone's home screen!

---

### Option 2: Build from Source (For Developers)

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/YOUR_GITHUB_USERNAME/GlanceCurrencyConverter.git](https://github.com/YOUR_GITHUB_USERNAME/GlanceCurrencyConverter.git)
   cd GlanceCurrencyConverter

2. **Open in Android Studio:
   Open Android Studio (Ladybug or newer recommended).

   Select Open and choose the cloned project directory.

   Build & Deploy:

   Connect your Android device via USB (or start an API 33+ Emulator).

   Press Shift + F10 or click the green Run ▶ button to build and install app-debug.apk.


## 🧮 Math & Conversion Strategy

Offline pair conversions are calculated dynamically against a cached USD base matrix using cross-rates:

<img width="920" height="164" alt="image" src="https://github.com/user-attachments/assets/79588d1b-37f6-4b9d-b0ff-1389adbea0ec" />


This approach keeps the cached storage lightweight (<50 KB) while supporting arbitrary conversions between 150+ global currencies offline.



## 🤝 Contributing

Contributions are welcome! If you'd like to improve the UI, add new features, or optimize offline caching:

  1. Fork the Project.
  2. Create your Feature Branch (git checkout -b feature/AmazingFeature).
  3. Commit your Changes (git commit -m 'Add some AmazingFeature').
  4. Push to the Branch (git push origin feature/AmazingFeature).
  5. Open a Pull Request.

## 📄 License

Distributed under the MIT License. See LICENSE for more information.

---

<ElicitationsGroup message="What would you like to update next for your repository?">
  <Elicitation label="Configure GitHub Actions to auto-build APKs on releases" query="Show how to set up a GitHub Actions workflow file to build and attach the release APK automatically when I tag a version."/>
  <Elicitation label="Guide me on how to rename the project/package to RoamRate later" query="How can I refactor my Android app package name and GitHub repo to RoamRate in the future without breaking builds?"/>
</ElicitationsGroup>

<FollowUp label="Need help adding a LICENSE file or setting up git?" query="How do I add an MIT license file to this repository from terminal?"/>
