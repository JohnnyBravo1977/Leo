# Little Genius (Leo App)

**Version:** V12 (Stable Active Build)  
**Type:** Offline/Online AI Chat Assistant  
**Language:** Kotlin (Jetpack Compose + Material 3)  
**Author:** Dad & Head Mistress

---

## 🧠 Overview
Little Genius is an intelligent, voice-ready assistant designed to work **both offline and online**.  
Built with Jetpack Compose and a persistent local data layer, Leo learns and adapts, handling messages, retries, themes, and more — all while keeping data stored locally.

---

## 🧩 Current Features
- Persistent chat history with `ChatStore` and `ChatRecord`
- Offline/online message retry logic
- Timestamps and delivery statuses
- Swipe-to-delete with undo
- Dynamic dark/light theme toggle
- Centered app title (“Little Genius”)
- Settings screen ready for extension
- Baseline UI built for Material 3

---

## 🗂️ Project Structure
app/ ├─ src/main/java/com/example/leo/ │ ├─ data/ │ │ ├─ ChatStore.kt │ │ ├─ ChatRecord.kt │ │ ├─ SyncStatus.kt │ │ └─ ThemeStore.kt │ ├─ ui/ │ │ ├─ ChatScreen.kt │ │ ├─ SettingsScreen.kt │ │ ├─ AppScaffold.kt │ │ ├─ Navigation.kt │ │ └─ theme/ │ │ ├─ AppTheme.kt │ │ ├─ Color.kt │ │ ├─ Type.kt │ │ └─ Theme.kt │ └─ MainActivity.kt ├─ res/ │ └─ drawable/, layout/, values/ ├─ AndroidManifest.xml ├─ build.gradle.kts ├─ settings.gradle.kts └─ gradle.properties


---

## 🔧 Build Setup
1. Clone the repo
   ```bash
   git clone https://github.com/YOUR-USERNAME/LeoApp.git
Open in Android Studio
Sync Gradle
Run app → target API 26+
🧭 Milestones
✅ V12 – Stable baseline (working chat, dark mode)
⏳ V13 – Swipe polish, top bar cleanup, Settings improvements
🚧 V14+ – Voice, sentiment, and AI integration
🧩 License
Private development build for personal use.
Copyright © 2025 Dad