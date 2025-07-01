# Kuripot App 🪙

**Personal Offline Budget + Notes Tracker for Android**

---

## 📋 Overview
Kuripot App is an offline-first personal budgeting and note-taking app for Android, designed to help freelancers and individuals manage their finances and journaling securely on their device. All data is stored locally, with optional export/import in JSON format. Built with **Kotlin**, **Jetpack Compose**, and **Room**.

---

## 🧹 Features
- **🔐 Passcode Lock:** 4-digit PIN required on app start, hashed locally
- **📝 Notes:** Categorized notes (journal, plan, etc.), with optional voice notes, edit/delete/archive
- **💰 Budget Tracker:** Add income/expenses, monthly totals, subcategories, linked to non-deletable Budget category
- **📂 Categories:** Add/edit/delete categories, classify notes and budgets
- **📁 Archives:** Deleted notes & budgets are archived, can be restored or permanently deleted
- **🌗 Theme:** Light & dark mode toggle, stored in local DB
- **🎧 Voice Notes:** Record and attach .m4a files to notes, playback in UI
- **🔄 Import/Export:** Export/import all app data as JSON

---

## 🛠️ Tech Stack
- **Kotlin** (100%)
- **Jetpack Compose** (UI)
- **Room** (SQLite ORM)
- **Kotlinx Serialization** (JSON import/export)
- **Material3** (UI components)
- **MediaRecorder/Media3** (Voice recording/playback)

---

## 🗃️ Architecture
- **MVVM** (ViewModel, Repository, Room DAO)
- **Single-activity Compose Navigation**
- **Local-only data privacy** (no cloud, no analytics)

**Main Packages:**
```
com.malikhain.kuripot_app
├── data/         // Room entities, DAOs, database
├── ui/           // Compose screens & dialogs
├── utils/        // Helper functions (theme, passcode, audio, JSON)
├── viewmodel/    // State & logic management
├── model/        // Data classes for JSON
├── service/      // Audio, import/export, archive
└── MainActivity.kt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest recommended)
- JDK 11+
- Android SDK 24+

### Setup
1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd KuripotApp
   ```
2. **Open in Android Studio**
3. **Sync Gradle** (auto or via `File > Sync Project with Gradle Files`)
4. **Build & Run** on an emulator or device (API 24+)

### Build/Run
- Use the standard Android Studio Run/Debug buttons
- The app will prompt for a 4-digit passcode on first launch
- All data is stored locally in Room DB

---

## 🧪 Testing
- Unit tests for DAOs in `app/src/test/`
- UI tests for navigation, passcode, and data state in `app/src/androidTest/`

---

## 🔐 Privacy
- **No cloud sync, no analytics, no ads**
- All data is local and can be exported/imported as JSON
- Passcode is hashed (SHA-256) and never stored in plain text

---

## 🤝 Contributing
1. Fork the repo & create your branch (`git checkout -b feature/your-feature`)
2. Commit your changes (`git commit -am 'Add new feature'`)
3. Push to the branch (`git push origin feature/your-feature`)
4. Open a Pull Request

**Please:**
- Follow Kotlin/Compose best practices
- Write clear commit messages
- Add/maintain tests for new features

---

## 📄 License
MIT (or specify your license here)

---

## 🙏 Credits
- Inspired by the needs of freelancers and privacy-focused users
- Built with ❤️ using modern Android tools 